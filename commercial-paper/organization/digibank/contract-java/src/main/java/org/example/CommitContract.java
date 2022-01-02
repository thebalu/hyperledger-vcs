/*
SPDX-License-Identifier: Apache-2.0
*/
package org.example;

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

    /**
     * Issue commercial paper
     *
     * @param {Context} ctx the transaction context
     * @param {String} issuer commercial paper issuer
     * @param {Integer} paperNumber paper number for this issuer
     * @param {String} issueDateTime paper issue date
     * @param {String} maturityDateTime paper maturity date
     * @param {Integer} faceValue face value of paper
     */
    @Transaction
    public Commit createCommit(CommitContext ctx, String committer, String commitHash, String commitDateTime,
                               int commitNumber, String changes) {

        System.out.println(ctx);

        Commit commit = Commit.createInstance(committer, commitHash, commitDateTime, commitNumber, changes);
        commit.setPending();
        System.out.println(commit);

        ctx.commitList.addCommit(commit);
        return commit;
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
