package org.biojava.nbio.core.util;

public class UncompressInputStreamData {
	public int[] tab_prefix;
	public byte[] tab_suffix;
	public int[] zeros;
	public byte[] stack;
	public boolean block_mode;
	public int n_bits;
	public int maxbits;
	public int maxmaxcode;
	public int maxcode;
	public int bitmask;
	public int oldcode;
	public byte finchar;
	public int stackp;
	public int free_ent;
	public byte[] data;
	public int bit_pos;
	public int end;
	public int got;
	public boolean eof;

	public UncompressInputStreamData(int[] zeros, byte[] data, int bit_pos, int end, int got, boolean eof) {
		this.zeros = zeros;
		this.data = data;
		this.bit_pos = bit_pos;
		this.end = end;
		this.got = got;
		this.eof = eof;
	}
}