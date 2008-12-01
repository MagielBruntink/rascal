package org.meta_environment.rascal.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.ITree;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.FactTypeError;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.AbstractAST;
import org.meta_environment.rascal.ast.Command;
import org.meta_environment.rascal.ast.Expression;
import org.meta_environment.rascal.ast.Module;
import org.meta_environment.rascal.ast.Statement;
import org.meta_environment.uptr.TreeAdapter;

/**
 * Uses reflection to construct an AST hierarchy from a 
 * UPTR parse tree of a rascal program.
 *
 */
public class ASTBuilder {
	private ASTFactory factory;
    private Class<? extends ASTFactory> clazz;
    
	public ASTBuilder(ASTFactory factory) {
		this.factory = factory;
		this.clazz = factory.getClass();
	}
	
	public Module buildModule(INode parseTree) throws FactTypeError {
		return buildSort(parseTree, "Module");
	}
	
	public Expression buildExpression(INode parseTree) {
		return buildSort(parseTree, "Expression");
	}
	
	public Statement buildStatement(INode parseTree) {
		return buildSort(parseTree, "Statement");
	}
	
	public Command buildCommand(INode parseTree) {
		return buildSort(parseTree, "Command");
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractAST> T buildSort(INode parseTree, String sort) {
		INode top = (INode) parseTree.get("top");
		TreeAdapter start = new TreeAdapter(top);
		INode tree = (INode) start.getArgs().get(1);
		TreeAdapter treeAdapter = new TreeAdapter(tree); 

		if (treeAdapter.getSortName().equals(sort)) {
			return (T) buildValue(tree);
		} else {
			throw new FactTypeError("This is not a" + sort +  ": "
					+ new TreeAdapter(parseTree).yield());
		}
	}
	
	private List<AbstractAST> buildList(INode in)  {
		IList args = new TreeAdapter(in).getListASTArgs();
		List<AbstractAST> result = new LinkedList<AbstractAST>();
		for (IValue arg: args) {
			result.add(buildValue(arg));
		}
		return result;
	}

	private AbstractAST buildContextFreeNode(INode in)  {
		try {
			TreeAdapter tree = new TreeAdapter(in);

			String cons = tree.getConstructorName();
			String sort = tree.getProduction().getSortName();
			sort = sort.equalsIgnoreCase("pattern") ? "Expression" : capitalize(sort); 
			cons = capitalize(cons);

			IList args = tree.getASTArgs();
			int arity = args.length() + 1;
			Class<?> formals[] = new Class<?>[arity];
			Object actuals[] = new Object[arity];

			formals[0] = ITree.class;
			actuals[0] = in;

			int i = 1;
			for (IValue arg : args) {
				TreeAdapter argTree = new TreeAdapter((INode) arg);
				if (argTree.isList()) {
					actuals[i] = buildList((INode) arg);
					formals[i] = List.class;
				}
				else {
					actuals[i] = buildValue(arg);
					formals[i] = actuals[i].getClass().getSuperclass();
				}
				i++;
			}

			Method make = clazz.getMethod("make" + sort + cons, formals);
			return (AbstractAST) make.invoke(factory, actuals);
		} catch (SecurityException e) {
			throw unexpectedError(e);
		} catch (NoSuchMethodException e) {
			throw unexpectedError(e);
		} catch (IllegalArgumentException e) {
			throw unexpectedError(e);
		} catch (IllegalAccessException e) {
			throw unexpectedError(e);
		} catch (InvocationTargetException e) {
			throw unexpectedError(e);
		}
	}
	
	private AbstractAST buildAmbNode(ITree tree, ISet alternatives) {
		try {
			String sort = null;
			List<AbstractAST> alts = new LinkedList<AbstractAST>();

			for (IValue elem : alternatives) {
				if (sort == null) {
					sort = new TreeAdapter((INode) elem).getSortName();
				}
				
				alts.add(buildValue(elem));
			}
			
			if (alts.size() == 0) {
				throw new FactTypeError("bug: Ambiguity without children!?! " + tree);
			}

			sort = capitalize(sort);
			Class<?> formals[] = new Class<?>[]  { ITree.class, List.class };
			Object actuals[] = new Object[] { tree, alts };

			Method make = clazz.getMethod("make" + sort + "Ambiguity", formals);
			return (AbstractAST) make.invoke(factory, actuals);
		} catch (SecurityException e) {
			throw unexpectedError(e);
		} catch (NoSuchMethodException e) {
			throw unexpectedError(e);
		} catch (IllegalArgumentException e) {
			throw unexpectedError(e);
		} catch (IllegalAccessException e) {
			throw unexpectedError(e);
		} catch (InvocationTargetException e) {
			throw unexpectedError(e);
		}
	}
	
	private AbstractAST buildLexicalNode(INode in) {
		try {
			TreeAdapter tree = new TreeAdapter(in);

			String sort = tree.getProduction().getSortName();
			String Sort = capitalize(sort);

			Class<?> formals[] = new Class<?>[] { ITree.class, String.class };
			Object actuals[] = new Object[] { in, new String(new TreeAdapter(in).yield()) };

			Method make = clazz.getMethod("make" + Sort + "Lexical", formals);
			return (AbstractAST) make.invoke(factory, actuals);
		} catch (SecurityException e) {
			throw unexpectedError(e);
		} catch (NoSuchMethodException e) {
			throw unexpectedError(e);
		} catch (IllegalArgumentException e) {
			throw unexpectedError(e);
		} catch (IllegalAccessException e) {
			throw unexpectedError(e);
		} catch (InvocationTargetException e) {
			throw unexpectedError(e);
		}
	}
	
	private FactTypeError unexpectedError(Throwable e) {
		return new FactTypeError("Unexpected error in AST construction", e);
	}

	private AbstractAST buildValue(IValue arg)  {
		TreeAdapter tree = new TreeAdapter((INode) arg);
		
		if (tree.isAmb()) {
			return buildAmbNode((ITree) arg, tree.getAlternatives());
		}
		if (!tree.isAppl()) {
			throw new UnsupportedOperationException();
		}	
		if (tree.isLexToCf()) {
			return buildLexicalNode((INode) ((IList) ((INode) arg).get("args")).get(0));
		}	
		return buildContextFreeNode((INode) arg);
	}



	private String capitalize(String sort) {
		if (sort.length() > 1) {
		  return Character.toUpperCase(sort.charAt(0)) + sort.substring(1);
		}
		else {
			return sort.toUpperCase();
		}
	}

}
