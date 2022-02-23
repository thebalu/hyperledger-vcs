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
    private String text;

    public static final String CURRENT_CONTENT_KEY = "current_content_key";

    public CurrentContent() {
        super();
    }

    @Override
    public String toString() {
        return "CurrentContent::" + this.key + "\n==========================" + this.getText() + "\n==========================";
    }

    public static CurrentContent deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        JSONObject json = new JSONObject(new String(data, UTF_8));

        String text = json.getString("text");
        return createInstance(text);
    }

    public static byte[] serialize(CurrentContent currentContent) {
        return State.serialize(currentContent);
    }

    public static CurrentContent createInstance(String text) {
        return new CurrentContent().setText(text).setKey();
    }

    public CurrentContent setKey() {
        this.key = CURRENT_CONTENT_KEY;
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
