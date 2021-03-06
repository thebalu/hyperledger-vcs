/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.example.ledgerapi.State;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class Commit extends State {

    @Property()
    private String commitHash;

    @Property()
    private String committer;

    @Property()
    private long commitDateTime;

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

    @Property()
    private ArrayList<String> approvingOrgs;

    @Property()
    private ArrayList<String> rejectingOrgs;

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

    public Commit setRejected() {
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

    public Long getCommitDateTime() {
        return commitDateTime;
    }

    public Commit setCommitDateTime(Long commitDateTime) {
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

    public String getChanges() {
        return this.changes;
    }

    @Override
    public String toString() {
        return "Commit::" + this.key + "   " + this.getCommitHash() + " " + getCommitter() + " " + getCommitNumber();
    }

    public Commit setApprovingOrgs(List<String> approvingOrgs) {
        this.approvingOrgs = new ArrayList<>(approvingOrgs);
        return this;
    }

    public List<String> getApprovingOrgs() {
        return approvingOrgs;
    }

    public Commit setRejectingOrgs(List<String> rejectingOrgs) {
        this.rejectingOrgs = new ArrayList<>(rejectingOrgs);
        return this;
    }

    public List<String> getRejectingOrgs() {
        return rejectingOrgs;
    }

    /**
     * Deserialize a state data to commercial paper
     *
     * @param {Buffer} data to form back into the object
     */
    public static Commit deserialize(byte[] data) {
        System.out.println("Deserializing data: ");
        System.out.println(new String(data, UTF_8));
        JSONObject json = new JSONObject(new String(data, UTF_8));
        System.out.println("Json: " + json);
        String committer = json.getString("committer");
        String changes = json.getString("changes");
        String commitHash = json.getString("commitHash");
        Long commitDateTime = json.getLong("commitDateTime");
        int commitNumber = json.getInt("commitNumber");
        JSONArray approvingOrgs = json.getJSONArray("approvingOrgs");
        JSONArray rejectingOrgs = json.getJSONArray("rejectingOrgs");
        String state = json.getString("state").toUpperCase();

        List<String> approvingOrgsList = approvingOrgs.toList()
                .stream().map(s -> (String) s).collect(Collectors.toList());
        List<String> rejectingOrgsList = rejectingOrgs.toList()
                .stream().map(s -> (String) s).collect(Collectors.toList());
        return createInstance(committer, commitHash, commitDateTime, commitNumber, changes, approvingOrgsList, rejectingOrgsList, state);
    }

    public static byte[] serialize(Commit paper) {
        return State.serialize(paper);
    }

    /**
     * Factory method to create a commercial paper object
     */
    public static Commit createInstance(String committer, String commitHash, Long commitDateTime,
                                        int commitNumber, String changes, List<String> approvingOrgsList,
                                        List<String> rejectingOrgsList, String state) {
        return new Commit()
                .setCommitter(committer)
                .setCommitHash(commitHash)
                .setCommitNumber(commitNumber)
                .setKey()
                .setCommitDateTime(commitDateTime)
                .setChanges(changes)
                .setApprovingOrgs(approvingOrgsList)
                .setRejectingOrgs(rejectingOrgsList)
                .setState(state);
    }

}
