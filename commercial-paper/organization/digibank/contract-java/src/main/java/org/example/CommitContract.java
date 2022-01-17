/*
SPDX-License-Identifier: Apache-2.0
*/
package org.example;

import lib.diff_match_patch;
import org.example.ledgerapi.State;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A custom context provides easy access to list of all commercial papers
 */

/**
 * Define commercial paper smart contract by extending Fabric Contract class
 *
 */
@Contract(name = "org.papernet.commit", info = @Info(title = "Commit contract", description = "", version = "0.0.1", license = @License(name = "SPDX-License-Identifier: ", url = ""), contact = @Contact(email = "java-contract@example.com", name = "java-contract", url = "http://java-contract.me")))
@Default
public class CommitContract implements ContractInterface {

    // use the classname for the logger, this way you can refactor
    private final static Logger LOG = Logger.getLogger(CommitContract.class.getName());

    private final diff_match_patch diffTool = new diff_match_patch();


    @Override
    public Context createContext(ChaincodeStub stub) {
        return new CommitContext(stub);
    }

    public CommitContract() {

    }

    /**
     * Define a custom context for commercial paper
     */

    /**
     * Instantiate to perform any setup of the ledger that might be required.
     *
     * @param {Context} ctx the transaction context
     */
    @Transaction
    public void instantiate(CommitContext ctx) {
        // No implementation required with this example
        // It could be where data migration is performed, if necessary
        LOG.info("No data migration to perform");
    }


    @Transaction
    public Commit createCommit(CommitContext ctx, String committer, String commitHash, int commitNumber, String changes) {

        LOG.info("Client identity msp: " + ctx.getClientIdentity().getMSPID());
        LOG.info("Client identity: " + ctx.getClientIdentity().getId());
        Commit commit = Commit.createInstance(committer, commitHash, ctx.getStub().getTxTimestamp(), commitNumber, changes,
                Collections.singletonList(ctx.getClientIdentity().getMSPID()));
        commit.setPending();
        LOG.info(commit.toString());

        ctx.commitList.addCommit(commit);
        return commit;
    }

    @Transaction
    public Commit voteApprove(CommitContext ctx, String commitHash) {
        LOG.info("Client identity msp: " + ctx.getClientIdentity().getMSPID());
        LOG.info("Client identity: " + ctx.getClientIdentity().getId());
        Commit commit = ctx.commitList.getCommit(commitHash);
        List<String> approvingOrgs = commit.getApprovingOrgs();
        String mspId = ctx.getClientIdentity().getMSPID();

        if (!commit.isPending()) {
            throw new RuntimeException("Commit is in " + commit.getState() + " state. Only PENDING commits can be voted on.");
        }
        if (approvingOrgs.contains(mspId)) {
            throw new RuntimeException("Org " + mspId + " has already approved this commit");
        }
        if (commit.getRejectingOrgs().contains(mspId)) {
            LOG.info("Changing vote from REJECT to APPROVE for org " + mspId);
            commit.getRejectingOrgs().remove(mspId);
        }else {
            LOG.info("APPROVING commit by org " + mspId);
        }
        approvingOrgs.add(mspId);

        Commit res = ctx.commitList.updateCommit(commit);
        LOG.info("Updated commit. \nApproving orgs list: [" + res.getApprovingOrgs() + "]\nRejecting org list: [" + res.getRejectingOrgs() + "]");
        return res;
    }

    @Transaction
    public Commit voteReject(CommitContext ctx, String commitHash) {
        LOG.info("Client identity msp: " + ctx.getClientIdentity().getMSPID());
        LOG.info("Client identity: " + ctx.getClientIdentity().getId());
        Commit commit = ctx.commitList.getCommit(commitHash);
        List<String> rejectingOrgs = commit.getRejectingOrgs();
        String mspId = ctx.getClientIdentity().getMSPID();

        if (!commit.isPending()) {
            throw new RuntimeException("Commit is in " + commit.getState() + " state. Only PENDING commits can be voted on.");
        }
        if (rejectingOrgs.contains(mspId)) {
            throw new RuntimeException("Org " + mspId + " has already rejected this commit");
        }
        if (commit.getApprovingOrgs().contains(mspId)) {
            LOG.info("Changing vote from APPROVE to REJECT for org " + mspId);
            commit.getApprovingOrgs().remove(mspId);
        } else {
            LOG.info("REJECTING commit by org " + mspId);
        }
        rejectingOrgs.add(mspId);

        Commit res = ctx.commitList.updateCommit(commit);
        LOG.info("Updated commit. \nApproving orgs list: [" + String.join(",", res.getApprovingOrgs())
                + "]\nRejecting org list: [" + String.join(",", res.getRejectingOrgs()) + "]");
        return res;
    }


    @Transaction
    public CurrentContent applyCommit(CommitContext ctx, String commitHash) {

        CurrentContent currentContent = ctx.commitList.getCurrentContent();
        String text = currentContent == null ? "" : currentContent.getText();


        LOG.info("Old content is:\n" + text);

        Commit commit = ctx.commitList.getCommit(commitHash);
        if (commit == null) {
            throw new RuntimeException("Commit with hash " + commitHash + " not found");
        }
        if (!commit.isPending()) {
            throw new RuntimeException("This commit has already been applied, it is in state " + commit.getState());
        }

        String diff = commit.getChanges();

        LOG.info("Diff contained in the commit is:\n" + diff);

        if (commit.getApprovingOrgs().size() < commit.getRejectingOrgs().size()) {
            throw new RuntimeException("Cannot apply commit, since it has fewer approvals: [" +
                    String.join(",", commit.getApprovingOrgs()) + "] than rejections: " + String.join(",", commit.getRejectingOrgs()));
        }

        // In the real world, this would be maybe a day
        if (ctx.getStub().getTxTimestamp().isBefore(commit.getCommitDateTime().plus(Duration.of(1, ChronoUnit.MINUTES)))) {
            throw new RuntimeException("Voting is still in progress. The voting period will end at " +
                    commit.getCommitDateTime().plus(Duration.of(1, ChronoUnit.MINUTES)).toString());
        }
        
        List<diff_match_patch.Patch> patches = diffTool.patch_fromText(diff);
        LinkedList<diff_match_patch.Patch> patchesLinkedList = new LinkedList<>(patches);

        Object[] result = diffTool.patch_apply(patchesLinkedList, text);
        String newText = (String) result[0];
        boolean[] success = (boolean[]) result[1];
        for (int i = 0; i < success.length; i++) {
            if (!success[i]) {
                throw new RuntimeException("Failed to apply patch " + i);
            }
        }

        LOG.info("Successfully applied patch. New content is:\n" + newText);

        commit.setApproved();
        ctx.commitList.updateCommit(commit);

        CurrentContent newContent = CurrentContent.createInstance(newText);
        ctx.commitList.updateCurrentContent(newContent);

        LOG.info("Updated current content");
        return newContent;
    }


}
