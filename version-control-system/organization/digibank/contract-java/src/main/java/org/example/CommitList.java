/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import org.example.ledgerapi.StateList;
import org.hyperledger.fabric.contract.Context;

public class CommitList {

    private final StateList stateList;
    private final StateList currentContentList;
    private final String CURRENT_CONTENT_LIST = "current_content_list";
    public CommitList(Context ctx) {
        this.stateList = StateList.getStateList(ctx, CommitList.class.getSimpleName(), Commit::deserialize);
        this.currentContentList = StateList.getStateList(ctx, CURRENT_CONTENT_LIST, CurrentContent::deserialize);
    }

    public CommitList addCommit(Commit commit) {
        stateList.addState(commit);
        return this;
    }

    public Commit getCommit(String commitHash) {
        return (Commit) this.stateList.getState(commitHash);
    }

    public CommitList updateCommit(Commit commit) {
        this.stateList.updateState(commit);
        return this;
    }

    public CurrentContent getCurrentContent() {
        return (CurrentContent) this.currentContentList.getState(CurrentContent.CURRENT_CONTENT_KEY);
    }

    public CommitList updateCurrentContent(CurrentContent currentContent) {
        this.currentContentList.updateState(currentContent);
        return this;
    }
}
