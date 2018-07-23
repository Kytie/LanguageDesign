import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class language {
    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if(args.length > 0)
                inputFile = args[0];
        
        InputStream is = System.in;
        if (inputFile != null)
                is = new FileInputStream(inputFile);

        ANTLRInputStream input = new ANTLRInputStream(is);
        GrammarLexer lexer = new GrammarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GrammarParser parser = new GrammarParser(tokens);

        ParseTree tree = parser.r();        
        EvalVisitor eval = new EvalVisitor();
        eval.visit(tree);
    }
}

