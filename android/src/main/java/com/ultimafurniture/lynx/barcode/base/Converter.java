package com.ultimafurniture.lynx.barcode.base;

/**
 * The convert of different integers.
 */
public class Converter {
	static final int LITTLE_ENDIAN = 0;
	static final int BIG_ENDIAN = 1;

	/**
	 * Convert int to byte Array.
	 * 
	 * @param number needed converter number
	 * @param order  the converted type little_endian or big_endian
	 * @return byte[] Converted byte array;
	 */
	public static byte[] getBytes(int number, int order) {
		int temp = number;
		byte[] b = new byte[4];

		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}

	/**
	 * Convert long to byte Array.
	 * 
	 * @param number needed converter number
	 * @param order  the converted type little_endian or big_endian
	 * @return byte[] Converted byte array;
	 */
	public static byte[] getBytes(long number, int order) {
		long temp = number;
		byte[] b = new byte[8];

		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}

	/**
	 * Convert short to byte Array.
	 * 
	 * @param number needed converter number
	 * @param order  the converted type little_endian or big_endian
	 * @return byte[] Converted byte array;
	 */
	public static byte[] getBytes(short number, int order) {
		int temp = number;
		byte[] b = new byte[8];

		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}

	/**
	 * Convert byte[] to long
	 * 
	 * @param b     the needed converter byte array.
	 * @param order the converter type little_endian or big_endian.
	 * @return long the converted number.
	 */
	public static long byteToLong(byte[] b, int order) {
		long s = 0;
		long s0 = b[0] & 0xff;
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;

		if (order == LITTLE_ENDIAN) {
			s1 <<= 8 * 1;
			s2 <<= 8 * 2;
			s3 <<= 8 * 3;
			s4 <<= 8 * 4;
			s5 <<= 8 * 5;
			s6 <<= 8 * 6;
			s7 <<= 8 * 7;
		} else {
			s0 <<= 8 * 7;
			s1 <<= 8 * 6;
			s2 <<= 8 * 5;
			s3 <<= 8 * 4;
			s4 <<= 8 * 3;
			s5 <<= 8 * 2;
			s6 <<= 8 * 1;
		}
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;

		return s;
	}

	/**
	 * Convert byte[] to int
	 * 
	 * @param b     the needed converter byte array.
	 * @param order the converter type little_endian or big_endian.
	 * @return int the converted number.
	 */
	public static int byteToInt(byte[] b, int order) {
		int s = 0;
		int s0 = b[0] & 0xff;//
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff;

		if (order == LITTLE_ENDIAN) {
			s2 <<= 8;
			s1 <<= 16;
			s0 <<= 24;
		} else {
			s3 <<= 24;
			s2 <<= 16;
			s1 <<= 8;
		}

		s = s0 | s1 | s2 | s3;

		return s;
	}

	/**
	 * Convert byte[] to short
	 * 
	 * @param b     the needed converter byte array.
	 * @param order the converter type little_endian or big_endian.
	 * @return short the converted number.
	 */
	public static short byteToShort(byte[] b, int order) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);//
		short s1 = (short) (b[1] & 0xff);

		if (order == LITTLE_ENDIAN) {
			s0 <<= 8;
			s = (short) (s0 | s1);
		} else {
			s1 <<= 8;
			s = (short) (s0 | s1);
		}
		return s;
	}
}
