/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.joni.ast;

import java.util.Set;

import org.joni.WarnCallback;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;

public final class ListNode extends Node {
    public Node value;
    public ListNode tail;

    private ListNode(Node value, ListNode tail, int type) {
        super(type);
        this.value = value;
        if (value != null) value.parent = this;
        this.tail = tail;
        if (tail != null) tail.parent = this;
    }

    public static ListNode newAlt(Node value, ListNode tail) {
        return new ListNode(value, tail, ALT);
    }

    public static ListNode newList(Node value, ListNode tail) {
        return new ListNode(value, tail, LIST);
    }

    public static ListNode listAdd(ListNode list, Node value) {
        ListNode n = newList(value, null);

        if (list != null) {
            while (list.tail != null) {
                list = list.tail;
            }
            list.setTail(n);
        }
        return n;
    }

    public void toListNode() {
        type = LIST;
    }

    public void toAltNode() {
        type = ALT;
    }

    @Override
    protected void setChild(Node newChild) {
        value = newChild;
    }

    @Override
    protected Node getChild() {
        return value;
    }

    @Override
    public void swap(Node with) {
        if (tail != null) {
            tail.parent = with;
            if (with instanceof ListNode) {
                ListNode withCan = (ListNode)with;
                withCan.tail.parent = this;
                ListNode tmp = tail;
                tail = withCan.tail;
                withCan.tail = tmp;
            }
        }
        super.swap(with);
    }

    @Override
    public void verifyTree(Set<Node> set, WarnCallback warnings) {
        if (!set.contains(this)) {
            set.add(this);
            if (value != null) {
                if (value.parent != this) {
                    warnings.warn("broken list value: " + this.getAddressName() + " -> " +  value.getAddressName());
                }
                value.verifyTree(set,warnings);
            }
            if (tail != null) {
                if (tail.parent != this) {
                    warnings.warn("broken list tail: " + this.getAddressName() + " -> " +  tail.getAddressName());
                }
                tail.verifyTree(set,warnings);
            }
        }
    }

    public Node setValue(Node ca) {
        value = ca;
        ca.parent = this;
        return value;
    }

    public ListNode setTail(ListNode cd) {
        tail = cd;
        cd.parent = this;
        return tail;
    }

    @Override
    public String getName() {
        switch (type) {
        case ALT:
            return "Alt";
        case LIST:
            return "List";
        default:
            throw new InternalException(ErrorMessages.ERR_PARSER_BUG);
        }
    }

    @Override
    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  value: " + pad(value, level + 1));
        sb.append("\n  tail: " + (tail == null ? "NULL" : tail.toString()));
        return sb.toString();
    }
}
