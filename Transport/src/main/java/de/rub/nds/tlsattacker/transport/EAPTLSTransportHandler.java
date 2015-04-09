/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS.
 *
 * Copyright (C) 2015 Juraj Somorovsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rub.nds.tlsattacker.transport;

import de.rub.nds.tlsattacker.eap.EapolMachine;
import de.rub.nds.tlsattacker.eap.ExtractTLS;
import de.rub.nds.tlsattacker.eap.FragState;
import de.rub.nds.tlsattacker.eap.NetworkHandler;
import de.rub.nds.tlsattacker.eap.SplitTLS;
import de.rub.nds.tlsattacker.util.ArrayConverter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author root
 */
class EAPTLSTransportHandler implements TransportHandler {

    private static final Logger LOGGER = LogManager.getLogger(EAPTLSTransportHandler.class);

    NetworkHandler nic = NetworkHandler.getInstance();

    EapolMachine eapolMachine = new EapolMachine();

    ExtractTLS extractor = new ExtractTLS();

    byte[] test;

    byte[] tlsraw = {};

    int y = 0, countpackets = 0;

    @Override
    public void initialize(String address, int port) throws IOException {
	nic.init();

	while (true) {

	    LOGGER.debug("initialize() send Frame: {}", eapolMachine.getState());
	    eapolMachine.send();
	    LOGGER.debug("initialize() receive Frame: {}", eapolMachine.getState());
	    test = eapolMachine.receive();

	    /*
	     * for (int i = 0; i < test.length; i++) { System.out.print(test[i]
	     * + " "); } System.out.println();
	     */

	    if (test[22] == 0x0d && test[23] == 0x20) {
		break;
	    }
	}
    }

    @Override
    public void sendData(byte[] data) throws IOException {

	SplitTLS fragment = SplitTLS.getInstance();
	if (data.length > 1024) {
	    eapolMachine.setState(new FragState(eapolMachine, eapolMachine.getID(), 0));
	    fragment.split(data);
	    countpackets = fragment.getCountPacket();
	    LOGGER.debug("sendData() SplitTLS packets: {}", Integer.toString(countpackets));
	}

	while (true) {

	    LOGGER.debug("sendData() send TLS-Frame: {}", eapolMachine.getState());
	    LOGGER.debug("sendData() send Fragment: {}", y);

	    if (eapolMachine.getState() == "HelloState") {
		eapolMachine.sendTLS(data);

		// Empfängt gleich das erste Server-Paket nach dem das letzte
		// Client-Paket versendet worden ist
		LOGGER.debug("sendData() receive TLS-Frame: {}", eapolMachine.getState());

		test = eapolMachine.receive();
		// und fügt es dem tlsraw Container hinzu
		tlsraw = ArrayConverter.concatenate(tlsraw, extractor.extract(test));

	    } else

	    if ((eapolMachine.getState() == "FragState" || eapolMachine.getState() == "HelloState")
		    && countpackets != 0) {
		eapolMachine.sendTLS(fragment.getFragment(y));
		y++;

		// Empfängt gleich das erste Server-Paket nach dem das letzte
		// Client-Paket versendet worden ist
		LOGGER.debug("sendData() receive TLS-Frame: {}", eapolMachine.getState());

		test = eapolMachine.receive();
		// und fügt es dem tlsraw Container hinzu
		tlsraw = ArrayConverter.concatenate(tlsraw, extractor.extract(test));

	    } else if (eapolMachine.getState() == "FragEndState" && countpackets != 0 && (countpackets - y != 0)) {
		eapolMachine.sendTLS(fragment.getFragment(y));
		y++;

		// Empfängt gleich das erste Server-Paket nach dem das letzte
		// Client-Paket versendet worden ist
		LOGGER.debug("sendData() receive TLS-Frame: {}", eapolMachine.getState());

		test = eapolMachine.receive();
		// und fügt es dem tlsraw Container hinzu
		tlsraw = ArrayConverter.concatenate(tlsraw, extractor.extract(test));

	    } else {
		eapolMachine.sendTLS(data);
		/*
		 * eapolMachine.sendTLS(fragment.getFragment(y)); y++;
		 */

	    }

	    /*
	     * for (int i = 0; i < test.length; i++) { System.out.print(test[i]
	     * + " "); } System.out.println();
	     */

	    LOGGER.debug("Fragments: {}", Integer.toString(y));

	    if (countpackets == y) {
		LOGGER.debug("All Fragments sent: {}", Integer.toString(countpackets));
		break;
	    }

	}
    }

    @Override
    public byte[] fetchData() throws IOException {

	while (true) {

	    // Code wird nur ausgeführt wenn Server Hello fragmentiert ist
	    if (countpackets != 0) {
		LOGGER.debug("fetchData() send Frame: {}", eapolMachine.getState());
		eapolMachine.send();

		LOGGER.debug("fetchData() receive Frame: {}", eapolMachine.getState());
		test = eapolMachine.receive();

		tlsraw = ArrayConverter.concatenate(tlsraw, extractor.extract(test));
	    }

	    if (test[23] != (byte) 0xc0) {
		LOGGER.debug("fetchData() send Frame or lastfragment: {}", eapolMachine.getState());
		eapolMachine.send();
		break;
	    }

	}

	return tlsraw;
    }

    @Override
    public void closeConnection() {
	nic.closeCon();
    }
}