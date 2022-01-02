/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.example.ledgerapi.State;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class Commit extends State {

    @Property()
    private String commitHash;

    @Property()
    private String committer;

    @Property()
    private String commitDateTime;

    @Property()
    private String changes;

    @Property()
    private int commitNumber;

    // Enumerate commercial paper state values
    public final static String PENDING = "PENDING";
    public final static String APPROVED = "APPROVED";
    public final static String REJECTED = "REJECTED";

    @Property()
    private String state=PENDING;


    public String getState() {
        return state;
    }

    public Commit setState(String state) {
        this.state = state;
        return this;
    }

    @JSONPropertyIgnore()
    public boolean isPending() {
        return this.state.equals(PENDING);
    }

    @JSONPropertyIgnore()
    public boolean isApproved() {
        return this.state.equals(APPROVED);
    }

    @JSONPropertyIgnore()
    public boolean isRejected() {
        return this.state.equals(REJECTED);
    }

    public Commit setPending() {
        this.state = PENDING;
        return this;
    }

    public Commit setApproved() {
        this.state = APPROVED;
        return this;
    }

    public Commit setRedeemed() {
        this.state = REJECTED;
        return this;
    }

    public Commit() {
        super();
    }

    public Commit setKey() {
        this.key = State.makeKey(new String[]{this.commitHash});
        return this;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public Commit setCommitHash(String commitHash) {
        this.commitHash = commitHash;
        return this;
    }

    public String getCommitter() {
        return committer;
    }

    public Commit setCommitter(String committer) {
        this.committer = committer;
        return this;
    }

    public String getCommitDateTime() {
        return commitDateTime;
    }

    public Commit setCommitDateTime(String commitDateTime) {
        this.commitDateTime = commitDateTime;
        return this;
    }

    public int getCommitNumber() {
        return commitNumber;
    }

    public Commit setCommitNumber(int commitNumber) {
        this.commitNumber = commitNumber;
        return this;
    }

    public Commit setChanges(String changes) {
        this.changes = changes;
        return this;
    }

    @Override
    public String toString() {
        return "Commit::" + this.key + "   " + this.getCommitHash() + " " + getCommitter() + " " + getCommitNumber();
    }

    /**
     * Deserialize a state data to commercial paper
     *
     * @param {Buffer} data to form back into the object
     */
    public static Commit deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String committer = json.getString("committer");
        String changes = json.getString("changes");
        String commitHash = json.getString("commitHash");
        String commitDateTime = json.getString("commitDateTime");
        int commitNumber = json.getInt("commitNumber");
        return createInstance(committer, commitHash, commitDateTime, commitNumber, changes);
    }

    public static byte[] serialize(Commit paper) {
        return State.serialize(paper);
    }

    /**
     * Factory method to create a commercial paper object
     */
    public static Commit createInstance(String committer, String commitHash, String commitDateTime,
                                        int commitNumber, String changes) {
        return new Commit().setCommitter(committer).setCommitHash(commitHash)
                .setCommitNumber(commitNumber).setKey().setCommitDateTime(commitDateTime).setChanges(changes);
    }

}
