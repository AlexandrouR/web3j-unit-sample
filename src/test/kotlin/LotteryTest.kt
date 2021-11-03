import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.web3j.EVMTest
import org.web3j.NodeType
import org.web3j.generated.contracts.Lottery
import org.web3j.protocol.Web3j
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider

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


}