/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.content.encoding;

/**
 * Structure storing encoding values from the {@code ACCEPT-ENCODING} HTTP header.
 */
public class ValuedEncoding implements Comparable<ValuedEncoding> {

    /**
     * The encoding.
     */
    String encoding = null;

    /**
     * The 'q' value (quality).
     */
    Double qValue = 1.0;

    /**
     * The position in the original header.
     */
    Integer position;

    /**
     * Creates a {@link org.wisdom.content.encoding.ValuedEncoding}.
     *
     * @param encodingName the encoding
     * @param qValue       the q value
     * @param position     the position in the header
     */
    public ValuedEncoding(String encodingName, Double qValue, int position) {
        this.encoding = encodingName;
        this.qValue = qValue;
        this.position = position;
    }

    /**
     * Parses the the given {@code ACCEPT-ENCODING} item (encodingItem), and creates an
     * {@link org.wisdom.content.encoding.ValuedEncoding}.
     *
     * @param encodingItem the item
     * @param position     the position
     */
    public ValuedEncoding(String encodingItem, int position) {
        this.position = position;
        //Split an encoding item between encoding and its qValue
        String[] encodingParts = encodingItem.split(";");
        //Grab encoding name
        encoding = encodingParts[0].trim().replace("\n", "");
        //Grab encoding's qValue if it exists (default 1.0 otherwise)
        if (encodingParts.length > 1) {
            qValue = Double.parseDouble(encodingParts[1].trim()
                    .replace("\n", "")
                    .replace("q=", ""));
        }
    }

    /**
     * Gets the encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets the 'q' value.
     *
     * @return the q value.
     */
    public Double getqValue() {
        return qValue;
    }

    /**
     * Gets the position from the original list.
     *
     * @return the position
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Compares two {@link org.wisdom.content.encoding.ValuedEncoding}.
     *
     * @param o the object to compare with.
     * @return 0, 1 or -1.
     */
    @Override
    public int compareTo(ValuedEncoding o) {
        if (qValue.equals(o.qValue)) {
            // In case 2 encodings have the same qValue, the first one has priority
            return position.compareTo(o.position);
        }
        //Highest qValue first, invert default ascending comparison
        return qValue.compareTo(o.qValue) * -1;
    }

    /**
     * Checks whether the current object is equal to the given object.
     *
     * @param obj the object
     * @return {@code true} if the two objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValuedEncoding)) {
            return false;
        }
        ValuedEncoding ov = (ValuedEncoding) obj;

        return this.getPosition().equals(ov.getPosition())
                && this.getEncoding().equals(ov.getEncoding()) && this.getqValue().equals(ov.getqValue());
    }

    /**
     * Hash code computation.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int enc = encoding != null ? encoding.hashCode() : 0;
        int qval = qValue != null ? qValue.hashCode() : 0;
        return 31 * (enc + qval + position);
    }
}