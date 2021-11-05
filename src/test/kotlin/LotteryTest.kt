import org.hyperledger.besu.ethereum.core.Wei
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.web3j.EVMTest
import org.web3j.NodeType
import org.web3j.evm.Configuration.Companion.TEST_ACCOUNTS
import org.web3j.generated.contracts.Lottery
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

@EVMTest(type = NodeType.EMBEDDED)
class LotteryTest {

    @Test
    fun `test contract was deployed`(
        web3j: Web3j,
        transactionManager: TransactionManager,
        contractGasProvider: ContractGasProvider
    ) {
        val lottery = Lottery.deploy(web3j, transactionManager, contractGasProvider).send()
        assertFalse(lottery.contractAddress.isNullOrEmpty())
    }

    @Test
    @Order(0)
    fun `test simple play`(
        web3j: Web3j,
        transactionManager: TransactionManager,
        contractGasProvider: ContractGasProvider
    ) {
        val managerAddress = TEST_ACCOUNTS.keys.toList()[0]
        val player1Address = TEST_ACCOUNTS.keys.toList()[1]

        val lottery = Lottery.deploy(web3j, transactionManager, contractGasProvider).send()

        assertFalse(lottery.contractAddress.isNullOrEmpty())
        assertEquals(managerAddress, lottery.manager().send())

        val managerInitialBalance = getBalance(web3j, managerAddress)
        val player1InitialBalance = getBalance(web3j, player1Address)

        web3j.ethSendTransaction(
            Transaction.createFunctionCallTransaction(
                player1Address,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.valueOf(Long.MAX_VALUE),
                lottery.contractAddress,
                Wei.fromEth(123).asBigInteger,
                lottery.enter(BigInteger.ZERO).encodeFunctionCall()
            )
        ).send()

        // Manager enters as a player as well :D
        lottery.enter(Wei.fromEth(1).asBigInteger).send()

        val players = lottery.players.send()
        assertEquals(2, players.size)

        lottery.pickWinner().send()

        val managerFinalBalance = getBalance(web3j, managerAddress)
        val player1FinalBalance = getBalance(web3j, player1Address)

        val managerDiff = managerFinalBalance - managerInitialBalance
        val player1Diff = player1FinalBalance - player1InitialBalance

        assertEquals(0, player1Diff.signum() + managerDiff.signum())

        println("""
P1  $player1Diff
i   $player1InitialBalance
f   $player1FinalBalance
-----
M   $managerDiff
i   $managerInitialBalance
f   $managerFinalBalance
         """)
    }

    @Test
    @Order(1)
    fun `test after simple play`(
        web3j: Web3j,
        transactionManager: TransactionManager,
        contractGasProvider: ContractGasProvider
    ) {
        println(getBalance(web3j, TEST_ACCOUNTS.keys.toList()[0]))
    }

    private fun getBalance(web3j: Web3j, managerAddress: String) =
        web3j.ethGetBalance(managerAddress) { "latest" }.send().balance

}