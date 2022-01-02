/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import org.example.ledgerapi.StateList;
import org.hyperledger.fabric.contract.Context;

public class CommitList {

    private StateList stateList;

    public CommitList(Context ctx) {
        this.stateList = StateList.getStateList(ctx, CommitList.class.getSimpleName(), Commit::deserialize);
    }

    public CommitList addCommit(Commit commit) {
        stateList.addState(commit);
        return this;
    }

    public Commit getCommit(String commitHash) {
        return (Commit) this.stateList.getState(commitHash);
    }

    public CommitList updatePaper(CommercialPaper paper) {
        this.stateList.updateState(paper);
        return this;
    }
}
