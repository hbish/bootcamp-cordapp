package bootcamp;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class TokenContract implements Contract {
    public static String ID = "bootcamp.TokenContract";

    public interface Commands extends CommandData {
        class Issue implements Commands { }
        class Transfer implements Commands { }
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandWithParties<TokenContract.Commands> command = requireSingleCommand(tx.getCommands(), TokenContract.Commands.class);
        if (command.getValue() instanceof Commands.Issue) {
            // checking shape
            if (tx.getInputStates().size() > 0)
                throw new IllegalArgumentException("Token issuance should have zero inputs.");
            if (tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("Token issuance should have one output.");

            TokenState inputState = tx.outputsOfType(TokenState.class).get(0);
            TokenState outputState = tx.outputsOfType(TokenState.class).get(0);

            if (outputState.getAmount() <= 0)
                throw new IllegalArgumentException("Token issurance should have an amojnt of more than 0");

            final List<PublicKey> requiredSigners = command.getSigners();
            if (!requiredSigners.contains(inputState.getIssuer().getOwningKey()))
                throw new IllegalArgumentException("Token issuance should have issuer as the required signer in the input");
            if (!requiredSigners.contains(outputState.getIssuer().getOwningKey()))
                throw new IllegalArgumentException("Token issuance should have issuer as the required signer in the output");

        } else {
            throw new IllegalArgumentException("Unexpected command for token contract");
        }
    }
}
