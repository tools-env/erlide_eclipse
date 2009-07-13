package org.erlide.wrangler.refactoring.util;

import com.ericsson.otp.erlang.JInterfaceFactory;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class Range implements IRange {
	protected int startLine, startCol, endLine, endCol;

	public Range(int startLine, int startCol, int endLine, int endCol) {
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
	}

	public Range(OtpErlangTuple position) throws OtpErlangRangeException {
		this((OtpErlangTuple) position.elementAt(0), (OtpErlangTuple) position
				.elementAt(1));
	}

	public Range(OtpErlangTuple startPos, OtpErlangTuple endPos)
			throws OtpErlangRangeException {
		this(((OtpErlangLong) startPos.elementAt(0)).intValue(),
				((OtpErlangLong) startPos.elementAt(1)).intValue(),
				((OtpErlangLong) endPos.elementAt(0)).intValue(),
				((OtpErlangLong) endPos.elementAt(1)).intValue());
	}

	@Override
	public int getEndCol() {
		return endCol;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}

	@Override
	public int getStartCol() {
		return startCol;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public OtpErlangTuple getStartPos() {
		return JInterfaceFactory.mkTuple(new OtpErlangInt(startLine),
				new OtpErlangInt(startCol));
	}

	@Override
	public OtpErlangTuple getEndPos() {
		return JInterfaceFactory.mkTuple(new OtpErlangInt(endLine),
				new OtpErlangInt(endCol));
	}

	@Override
	public String toString() {
		return "{" + getStartLine() + "," + getStartCol() + "}" + "-" + "{"
				+ getEndLine() + "," + getEndCol() + "}";
	}

	@Override
	public OtpErlangTuple getPos() {
		return JInterfaceFactory.mkTuple(getStartPos(), getEndPos());
	}
}