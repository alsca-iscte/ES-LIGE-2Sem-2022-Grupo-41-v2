/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
/*
 * @(#)UncompressInputStream.java           0.3-3 06/05/2001
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-2001 Ronald Tschalar
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@xxxxxxxxxxxxx
 *
 *  The HTTPClient's home page is located at:
 *
 *  http://www.innovation.ch/java/HTTPClient/
 */

package org.biojava.nbio.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * This class decompresses an input stream containing data compressed with
 * the unix "compress" utility (LZC, a LZW variant). This code is based
 * heavily on the <var>unlzw.c</var> code in <var>gzip-1.2.4</var> (written
 * by Peter Jannesen) and the original compress code.
 *
 *
 *  This version has been modified from the original 0.3-3 version by the
 *  Unidata Program Center (support@xxxxxxxxxxxxxxxx) to make the constructor
 *  public and to fix a couple of bugs.
 *  Also:
 *   - markSupported() returns false
 *   - add uncompress() static method
 *
 * @version 0.3-3 06/05/2001
 * @author  Ronald Tschalar
 * @author  Unidata Program Center
 * @author  Richard Holland - making LZW_MAGIC package-visible.
 *
 * @version 0.3-5 2008/01/19
 * @author  Fred Hansen (zweibieren@yahoo.com)
 * 	Fixed available() and the EOF condition for mainloop.
 * 	Also added some comments.
 *
 * @version 1.0 2018/01/08
 * @author      Fred Hansen (zweibieren@yahoo.com)
 * 	added uncompress(InputStream,OutputStream)
 * 	    and called it from main(String[])
 * 	    and uncompress(String, FileOutputStream)
 * 	normalize indentation
 * 	rewrite skip method
 * 	amend logging code in uncompress(String, FileOutputStream)
 */
public class UncompressInputStream extends FilterInputStream {
	private final static Logger logger
		= LoggerFactory.getLogger(UncompressInputStream.class);

	/**
	 * @param is the input stream to decompress
	 * @throws IOException if the header is malformed
	 */
	public UncompressInputStream(InputStream is) throws IOException {
		super(is);
		parse_header();
	}


	@Override
	public synchronized int read() throws IOException {
		byte[] one = new byte[1];
		int b = read(one, 0, 1);
		if (b == 1)
			return (one[0] & 0xff);
		else
			return -1;
	}


	// string table stuff
	private static final int TBL_CLEAR = 0x100;
	private static final int TBL_FIRST = TBL_CLEAR + 1;

	private UncompressInputStreamData data = new UncompressInputStreamData(new int[256], new byte[10000], 0, 0, 0, false);
	private static final int EXTRA = 64;


	@Override
	public synchronized int read(byte[] buf, int off, int len)
			throws IOException {
		if (data.eof) return -1;
		int start = off;

		/* Using local copies of various variables speeds things up by as
		 * much as 30% !
		 */
		int[] l_tab_prefix = data.tab_prefix;
		byte[] l_tab_suffix = data.tab_suffix;
		byte[] l_stack = data.stack;
		int l_n_bits = data.n_bits;
		int l_maxcode = data.maxcode;
		int l_maxmaxcode = data.maxmaxcode;
		int l_bitmask = data.bitmask;
		int l_oldcode = data.oldcode;
		byte l_finchar = data.finchar;
		int l_stackp = data.stackp;
		int l_free_ent = data.free_ent;
		byte[] l_data = data.data;
		int l_bit_pos = data.bit_pos;

		// empty stack if stuff still left
		int s_size = l_stack.length - l_stackp;
		if (s_size > 0) {
			int num = (s_size >= len) ? len : s_size;
			System.arraycopy(l_stack, l_stackp, buf, off, num);
			off += num;
			len -= num;
			l_stackp += num;
		}

		if (len == 0) {
			data.stackp = l_stackp;
			return off - start;
		}

		// loop, filling local buffer until enough data has been decompressed
		main_loop: do {
			if (data.end < EXTRA) fill();

			int bit_end = (data.got > 0)
			    ?  (data.end - data.end % l_n_bits) << 3  	// set to a "chunk" boundary
			    :  (data.end << 3) - (l_n_bits - 1);  	// no more data, set to last code

			while (l_bit_pos < bit_end) {		// handle 1-byte reads correctly
				if (len == 0) {
					data.n_bits = l_n_bits;
					data.maxcode = l_maxcode;
					data.maxmaxcode = l_maxmaxcode;
					data.bitmask = l_bitmask;
					data.oldcode = l_oldcode;
					data.finchar = l_finchar;
					data.stackp = l_stackp;
					data.free_ent = l_free_ent;
					data.bit_pos = l_bit_pos;

					return off - start;
				}

				// check for code-width expansion

				if (l_free_ent > l_maxcode) {
					int n_bytes = l_n_bits << 3;
					l_bit_pos = (l_bit_pos - 1) +
							n_bytes - (l_bit_pos - 1 + n_bytes) % n_bytes;

					l_n_bits++;
					l_maxcode = (l_n_bits == data.maxbits) ? l_maxmaxcode :
							(1 << l_n_bits) - 1;

					logger.debug("Code-width expanded to ", l_n_bits);

					l_bitmask = (1 << l_n_bits) - 1;
					l_bit_pos = resetbuf(l_bit_pos);
					continue main_loop;
				}


				// read next code

				int pos = l_bit_pos >> 3;
				int code = (((l_data[pos] & 0xFF) | ((l_data[pos + 1] & 0xFF) << 8) |
						((l_data[pos + 2] & 0xFF) << 16))
						>> (l_bit_pos & 0x7)) & l_bitmask;
				l_bit_pos += l_n_bits;


				// handle first iteration

				if (l_oldcode == -1) {
					if (code >= 256)
						throw new IOException("corrupt input: " + code +
								" > 255");
					l_finchar = (byte) (l_oldcode = code);
					buf[off++] = l_finchar;
					len--;
					continue;
				}


				// handle CLEAR code

				s_size = s_size(l_tab_prefix, l_tab_suffix, l_stack, l_oldcode, l_finchar, l_stackp, l_free_ent, s_size,
						code);
				if (code == TBL_CLEAR && data.block_mode) {
					System.arraycopy(data.zeros, 0, l_tab_prefix, 0, data.zeros.length);
					l_free_ent = TBL_FIRST - 1;

					int n_bytes = l_n_bits << 3;
					l_bit_pos = (l_bit_pos - 1) +
							n_bytes - (l_bit_pos - 1 + n_bytes) % n_bytes;
					l_n_bits = INIT_BITS;
					l_maxcode = (1 << l_n_bits) - 1;
					l_bitmask = l_maxcode;

					logger.debug("Code tables reset");

					l_bit_pos = resetbuf(l_bit_pos);
					continue main_loop;
				}


				// setup

				int incode = code;
				l_stackp = l_stack.length;


				// Handle KwK case

				if (code >= l_free_ent) {
					if (code > l_free_ent)
						throw new IOException("corrupt input: code=" + code +
								", free_ent=" + l_free_ent);

					l_stack[--l_stackp] = l_finchar;
					code = l_oldcode;
				}


				// Generate output characters in reverse order

				while (code >= 256) {
					l_stack[--l_stackp] = l_tab_suffix[code];
					code = l_tab_prefix[code];
				}
				l_finchar = l_tab_suffix[code];
				buf[off++] = l_finchar;
				len--;


				// And put them out in forward order

				int num = (s_size >= len) ? len : s_size;
				System.arraycopy(l_stack, l_stackp, buf, off, num);
				off += num;
				len -= num;
				l_stackp += num;


				// generate new entry in table

				l_tab_prefix = l_tab_prefix(l_tab_prefix, l_maxmaxcode, l_oldcode, l_free_ent);
				if (l_free_ent < l_maxmaxcode) {
					l_tab_suffix[l_free_ent] = l_finchar;
					l_free_ent++;
				}


				// Remember previous code

				l_oldcode = incode;


				// if output buffer full, then return

				if (len == 0) {
					data.n_bits = l_n_bits;
					data.maxcode = l_maxcode;
					data.bitmask = l_bitmask;
					data.oldcode = l_oldcode;
					data.finchar = l_finchar;
					data.stackp = l_stackp;
					data.free_ent = l_free_ent;
					data.bit_pos = l_bit_pos;

					return off - start;
				}
			}

			l_bit_pos = resetbuf(l_bit_pos);
		} while
	   		// old code: (got>0)  fails if code width expands near EOF
	   		(data.got > 0	    // usually true
			|| l_bit_pos < (data.end << 3) - (l_n_bits - 1));  // last few bytes

		data.n_bits = l_n_bits;
		data.maxcode = l_maxcode;
		data.bitmask = l_bitmask;
		data.oldcode = l_oldcode;
		data.finchar = l_finchar;
		data.stackp = l_stackp;
		data.free_ent = l_free_ent;
		data.bit_pos = l_bit_pos;

		data.eof = true;
		return off - start;
	}


	private int s_size(int[] l_tab_prefix, byte[] l_tab_suffix, byte[] l_stack, int l_oldcode, byte l_finchar,
			int l_stackp, int l_free_ent, int s_size, int code) throws IOException {
		if (code == TBL_CLEAR && data.block_mode) {
			l_free_ent = TBL_FIRST - 1;
		}
		l_stackp = l_stack.length;
		if (code >= l_free_ent) {
			if (code > l_free_ent)
				throw new IOException("corrupt input: code=" + code + ", free_ent=" + l_free_ent);
			l_stack[--l_stackp] = l_finchar;
			code = l_oldcode;
		}
		while (code >= 256) {
			l_stack[--l_stackp] = l_tab_suffix[code];
			code = l_tab_prefix[code];
		}
		s_size = l_stack.length - l_stackp;
		return s_size;
	}


	private int[] l_tab_prefix(int[] l_tab_prefix, int l_maxmaxcode, int l_oldcode, int l_free_ent) {
		if (l_free_ent < l_maxmaxcode) {
			l_tab_prefix[l_free_ent] = l_oldcode;
		}
		return l_tab_prefix;
	}


	/**
	 * Moves the unread data in the buffer to the beginning and resets
	 * the pointers.
	 */
	private int resetbuf(int bit_pos) {
		int pos = bit_pos >> 3;
		System.arraycopy(data.data, pos, data.data, 0, data.end - pos);
		data.end -= pos;
		return 0;
	}


	private void fill() throws IOException {
		data.got = in.read(data.data, data.end, data.data.length - 1 - data.end);
		if (data.got > 0) data.end += data.got;
	}


	@Override
	public synchronized long skip(long num) throws IOException {
		return Math.max(0, read(new byte[(int) num]));
	}


	@Override
	public synchronized int available() throws IOException {
		if (data.eof) return 0;
		// the old code was:    return in.available();
		// it fails because this.read() can return bytes
		// even after in.available()  is zero
		// -- zweibieren
		int avail = in.available();
		return (avail == 0) ? 1 : avail;
	}


	static final int LZW_MAGIC = 0x1f9d;
	private static final int MAX_BITS = 16;
	private static final int INIT_BITS = 9;
	private static final int HDR_MAXBITS = 0x1f;
	private static final int HDR_EXTENDED = 0x20;
	private static final int HDR_FREE = 0x40;
	private static final int HDR_BLOCK_MODE = 0x80;

	private void parse_header() throws IOException {
		// read in and check magic number
		int t = in.read();
		if (t < 0) throw new EOFException("Failed to read magic number");
		int magic = (t & 0xff) << 8;
		t = in.read();
		if (t < 0) throw new EOFException("Failed to read magic number");
		magic += t & 0xff;
		if (magic != LZW_MAGIC)
			throw new IOException("Input not in compress format (read " +
					"magic number 0x" +
					Integer.toHexString(magic) + ")");

		// read in header byte
		int header = in.read();
		if (header < 0) throw new EOFException("Failed to read header");

		data.block_mode = (header & HDR_BLOCK_MODE) > 0;
		data.maxbits = header & HDR_MAXBITS;

		if (data.maxbits > MAX_BITS)
			throw new IOException("Stream compressed with " + data.maxbits +
					" bits, but can only handle " + MAX_BITS +
					" bits");

		if ((header & HDR_EXTENDED) > 0)
			throw new IOException("Header extension bit set");

		if ((header & HDR_FREE) > 0)
			throw new IOException("Header bit 6 set");

		logger.debug("block mode: {}", data.block_mode);
		logger.debug("max bits:   {}", data.maxbits);

		// initialize stuff
		data.maxmaxcode = 1 << data.maxbits;
		data.n_bits = INIT_BITS;
		data.maxcode = (1 << data.n_bits) - 1;
		data.bitmask = data.maxcode;
		data.oldcode = -1;
		data.finchar = 0;
		data.free_ent = data.block_mode ? TBL_FIRST : 256;

		data.tab_prefix = new int[1 << data.maxbits];
		data.tab_suffix = new byte[1 << data.maxbits];
		data.stack = new byte[1 << data.maxbits];
		data.stackp = data.stack.length;

		for (int idx = 255; idx >= 0; idx--)
			data.tab_suffix[idx] = (byte) idx;
	}

	/**
	 * This stream does not support mark/reset on the stream.
	 *
	 * @return false
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	/**
	 * Read a named file and uncompress it.
	 * @param fileInName Name of compressed file.
	 * @param out A destination for the result. It is closed after data is sent.
	 * @return number of bytes sent to the output stream,
	 * @throws IOException for any error
	 */
	public static long uncompress(String fileInName, FileOutputStream out)
			throws IOException {
		long start = System.currentTimeMillis();
		long total;
		try (InputStream fin = new FileInputStream(fileInName)) {
			total = uncompress(fin, out);
		}
		out.close();

		if (debugTiming) {
			long end = System.currentTimeMillis();
			logger.info("Decompressed {} bytes", total);
			UncompressInputStream.logger.info("Time: {} seconds", (end - start) / 1000);
		}
		return total;
	}

	/**
	* Read an input stream and uncompress it to an output stream.
	* @param in the incoming InputStream. It is NOT closed.
	* @param out the destination OutputStream. It is NOT closed.
	* @return number of bytes sent to the output stream
	* @throws IOException for any error
	*/
	public static long uncompress(InputStream in, OutputStream out)
		throws IOException {
		UncompressInputStream ucis = new UncompressInputStream(in);
		long total = 0;
		byte[] buffer = new byte[100000];
		while (true) {
			int bytesRead = ucis.read(buffer);
			if (bytesRead == -1) break;
			out.write(buffer, 0, bytesRead);
			total += bytesRead;
		}
		return total;
	}

	private static final boolean debugTiming = false;

	
}
