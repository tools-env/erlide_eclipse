package org.erlide.wrangler.refactoring.backend.internal;

import java.util.ArrayList;

import org.erlide.wrangler.refactoring.backend.ChangedFile;
import org.erlide.wrangler.refactoring.backend.IRefactoringRpcMessage;


import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public abstract class AbstractRefactoringRpcMessage extends AbstractRpcMessage
		implements IRefactoringRpcMessage {

	protected ArrayList<ChangedFile> changedFiles = null;

	@Override
	public ArrayList<ChangedFile> getRefactoringChangeset() {
		return changedFiles;
	}

	protected ArrayList<ChangedFile> parseFileList(OtpErlangList fileList) {
		ArrayList<ChangedFile> ret = new ArrayList<ChangedFile>();

		OtpErlangTuple e;
		OtpErlangString oldPath, newPath, newContent;
		for (int i = 0; i < fileList.arity(); ++i) {
			e = (OtpErlangTuple) fileList.elementAt(i);
			oldPath = (OtpErlangString) e.elementAt(0);
			newPath = (OtpErlangString) e.elementAt(1);
			newContent = (OtpErlangString) e.elementAt(2);

			ret.add(new ChangedFile(oldPath.stringValue(), newPath
					.stringValue(), newContent.stringValue()));
		}
		return ret;
	}

}