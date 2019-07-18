/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.permissioning;

import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.account.TransferTransaction;

import java.math.BigInteger;
import java.util.Arrays;

public class AccountLocalAndOnChainPermissioningAcceptanceTest extends
        AccountSmartContractPermissioningAcceptanceTestBase {

    private Node node;
    private Account senderC;

    @Before
    public void setUp() {
        senderC = accounts.createAccount("Account-C");
    }

    @Test
    public void testAccountCannotSendTxWhenNotOnLocalWhiteList() {
        //OnChain: Primary, Secondary, C
        //Local: Primary, Secondary

        node = permissionedNode("node1",
                Arrays.asList(accounts.getPrimaryBenefactor().getAddress(),
                        accounts.getSecondaryBenefactor().getAddress()));
        permissionedCluster.start(node);

        // ensure SenderC has got some ether available
        node.execute(accountTransactions.createTransfer(senderC, 10));
        node.verify(senderC.balanceEquals(10));

        //add accounts to onChain Whitelist
        node.execute(allowAccount(accounts.getPrimaryBenefactor()));
        node.verify(accountIsAllowed(accounts.getPrimaryBenefactor()));

        node.execute(allowAccount(accounts.getSecondaryBenefactor()));
        node.verify(accountIsAllowed(accounts.getSecondaryBenefactor()));

        node.execute(allowAccount(senderC));
        node.verify(accountIsAllowed(senderC));

        //sender C should not be able to send Tx
        verifyTransferForbidden(senderC, accounts.getSecondaryBenefactor());
    }

    @Test
    public void testAccountCannotSendTxWhenNotOnOnChainWhiteList() {
        //OnChain Primary, Secondary
        //Local Primary, Secondary, C

    }

    private void verifyTransferForbidden(final Account sender, final Account beneficiary) {
        BigInteger nonce = node.execute(ethTransactions.getTransactionCount(sender.getAddress()));
        TransferTransaction transfer =
                accountTransactions.createTransfer(sender, beneficiary, 1, nonce);
        node.verify(
                eth.sendRawTransactionExceptional(
                        transfer.signedTransactionData(),
                        "Sender account not authorized to send transactions"));
    }
}
