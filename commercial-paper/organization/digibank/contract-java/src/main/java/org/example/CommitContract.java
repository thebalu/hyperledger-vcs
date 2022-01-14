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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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
    public Commit createCommit(CommitContext ctx, String committer, String commitHash, String commitDateTime,
                               int commitNumber, String changes) {

        System.out.println(ctx);
        System.out.println("Client identity msp: " + ctx.getClientIdentity().getMSPID());
        System.out.println("Client identity: " + ctx.getClientIdentity().getId());
        Commit commit = Commit.createInstance(committer, commitHash, commitDateTime, commitNumber, changes);
        commit.setPending();
        System.out.println(commit);

        ctx.commitList.addCommit(commit);
        return commit;
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
        String diff = commit.getChanges();

        LOG.info("Diff contained in the commit is:\n" + diff);

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

        CurrentContent newContent = CurrentContent.createInstance(commit, newText);
        ctx.commitList.updateCurrentContent(newContent);

        LOG.info("Updated current content");
        return newContent;
    }

//    @Transaction
//    public Commit approve(CommitContext ctx, String issuer, String paperNumber, String currentOwner,
//            String newOwner, int price, String purchaseDateTime) {
//
//        // Retrieve the current paper using key fields provided
//        String paperKey = State.makeKey(new String[] { paperNumber });
//        Commit paper = ctx.commitList.getCommit(paperKey);
//
//        // Validate current owner
//        if (!paper.getOwner().equals(currentOwner)) {
//            throw new RuntimeException("Paper " + issuer + paperNumber + " is not owned by " + currentOwner);
//        }
//
//        // First buy moves state from ISSUED to TRADING
//        if (paper.isIssued()) {
//            paper.setTrading();
//        }
//
//        // Check paper is not already REDEEMED
//        if (paper.isTrading()) {
//            paper.setOwner(newOwner);
//        } else {
//            throw new RuntimeException(
//                    "Paper " + issuer + paperNumber + " is not trading. Current state = " + paper.getState());
//        }
//
//        // Update the paper
//        ctx.paperList.updatePaper(paper);
//        return paper;
//    }

    /**
     * Redeem commercial paper
     *
     * @param {Context} ctx the transaction context
     * @param {String} issuer commercial paper issuer
     * @param {Integer} paperNumber paper number for this issuer
     * @param {String} redeemingOwner redeeming owner of paper
     * @param {String} redeemDateTime time paper was redeemed
     */
    @Transaction
    public CommercialPaper redeem(CommercialPaperContext ctx, String issuer, String paperNumber, String redeemingOwner,
            String redeemDateTime) {

        String paperKey = CommercialPaper.makeKey(new String[] { paperNumber });

        CommercialPaper paper = ctx.paperList.getPaper(paperKey);

        // Check paper is not REDEEMED
        if (paper.isRedeemed()) {
            throw new RuntimeException("Paper " + issuer + paperNumber + " already redeemed");
        }

        // Verify that the redeemer owns the commercial paper before redeeming it
        if (paper.getOwner().equals(redeemingOwner)) {
            paper.setOwner(paper.getIssuer());
            paper.setRedeemed();
        } else {
            throw new RuntimeException("Redeeming owner does not own paper" + issuer + paperNumber);
        }

        ctx.paperList.updatePaper(paper);
        return paper;
    }

}
