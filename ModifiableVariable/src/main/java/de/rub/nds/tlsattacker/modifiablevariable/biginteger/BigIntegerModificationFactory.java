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
package de.rub.nds.tlsattacker.modifiablevariable.biginteger;

import de.rub.nds.tlsattacker.modifiablevariable.VariableModification;
import de.rub.nds.tlsattacker.util.RandomHelper;
import java.math.BigInteger;
import java.util.Random;

/**
 * @author
 */
final public class BigIntegerModificationFactory {

    private static final int MODIFICATION_COUNT = 4;

    private static final int MAX_MODIFICATION_VALUE = 32000;

    private BigIntegerModificationFactory() {
    }

    public static BigIntegerAddModification add(final String summand) {
	return add(new BigInteger(summand));
    }

    public static BigIntegerAddModification add(final BigInteger summand) {
	return new BigIntegerAddModification(summand);
    }

    public static VariableModification<BigInteger> sub(final String subtrahend) {
	return sub(new BigInteger(subtrahend));
    }

    public static VariableModification<BigInteger> sub(final BigInteger subtrahend) {
	return new BigIntegerSubtractModification(subtrahend);
    }

    public static VariableModification<BigInteger> xor(final String xor) {
	return xor(new BigInteger(xor));
    }

    public static VariableModification<BigInteger> xor(final BigInteger xor) {
	return new BigIntegerXorModification(xor);
    }

    public static VariableModification<BigInteger> explicitValue(final String value) {
	return explicitValue(new BigInteger(value));
    }

    public static VariableModification<BigInteger> explicitValue(final BigInteger value) {
	return new BigIntegerExplicitValueModification(value);
    }

    public static VariableModification<BigInteger> createRandomModification() {
	Random random = RandomHelper.getRandom();
	int r = random.nextInt(MODIFICATION_COUNT);
	BigInteger modification = BigInteger.valueOf(random.nextInt(MAX_MODIFICATION_VALUE));
	VariableModification<BigInteger> vm = null;
	switch (r) {
	    case 0:
		vm = new BigIntegerAddModification(modification);
		return vm;
	    case 1:
		vm = new BigIntegerSubtractModification(modification);
		return vm;
	    case 2:
		vm = new BigIntegerXorModification(modification);
		return vm;
	    case 3:
		vm = new BigIntegerExplicitValueModification(modification);
		return vm;
	}
	return vm;
    }
}