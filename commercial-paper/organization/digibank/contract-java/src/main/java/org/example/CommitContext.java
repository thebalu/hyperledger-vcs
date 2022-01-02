package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

class CommitContext extends Context {

    public CommitContext(ChaincodeStub stub) {
        super(stub);
        this.commitList = new CommitList(this);
    }

    public CommitList commitList;

}