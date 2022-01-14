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
public class CurrentContent extends State {

    @Property()
    private Commit latestCommit;

    @Property()
    private String text;

    public static final String CURRENT_CONTENT_KEY = "current_content_key";

    public CurrentContent() {
        super();
    }

    @Override
    public String toString() {
        return "CurrentContent::" + this.key + "\n==========================" + this.getText() + "\n==========================";
    }

    /**
     * Deserialize a state data to commercial paper
     *
     * @param {Buffer} data to form back into the object
     */
    public static CurrentContent deserialize(byte[] data) {
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String latestCommitString = json.getString("latestCommit");
        Commit latestCommit = Commit.deserialize(latestCommitString.getBytes(UTF_8));
        String text = json.getString("text");
        return createInstance(latestCommit, text);
    }

    public static byte[] serialize(CurrentContent paper) {
        return State.serialize(paper);
    }

    public static CurrentContent createInstance(Commit latestCommit, String text) {
        return new CurrentContent().setLatestCommit(latestCommit).setText(text).setKey();
    }

    public CurrentContent setKey() {
        this.key = CURRENT_CONTENT_KEY;
        return this;
    }

    public Commit getLatestCommit() {
        return latestCommit;
    }

    public CurrentContent setLatestCommit(Commit latestCommit) {
        this.latestCommit = latestCommit;
        return this;
    }

    public String getText() {
        return text;
    }

    public CurrentContent setText(String text) {
        this.text = text;
        return this;
    }
}
