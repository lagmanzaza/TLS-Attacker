/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS.
 *
 * Copyright (C) 2015 Felix Lange
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
package de.rub.nds.tlsattacker.eap;

public class FragStartState implements EapState {

    EapolMachine eapolMachine;

    int id;

    EapFactory eaptlsfactory = new EapTlsFactory();

    NetworkHandler nic = NetworkHandler.getInstance();

    byte[] data = {};

    public FragStartState(EapolMachine eapolMachine, int id) {

	this.eapolMachine = eapolMachine;
	this.id = id;

    }

    @Override
    public void send() {

	EAPFrame eapstart = eaptlsfactory.createFrame("EAPTLSFRAGACK", id);
	nic.sendFrame(eapstart.getFrame());

    }

    @Override
    public void sendTLS(byte[] tlspacket) {

	EAPFrame eapstart = eaptlsfactory.createFrame("EAPTLSFRAG", id, tlspacket);
	nic.sendFrame(eapstart.getFrame());

    }

    @Override
    public byte[] receive() {
	data = nic.receiveFrame();
	id = (int) data[19]; // Get ID

	if (data[23] == (byte) 0xc0 || data[23] == (byte) 0x40 || data[23] == (byte) 0x00) {
	    eapolMachine.setState(new FragStartState(eapolMachine, id));
	} else {
	    eapolMachine.setState(new FragState(eapolMachine, id, 1));
	}
	return data;
    }

    public String getState() {
	return "FragStartState";
    }

    @Override
    public int getID() {

	return id;

    }

}