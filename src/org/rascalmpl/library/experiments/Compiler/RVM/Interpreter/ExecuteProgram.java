package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter;

import java.io.IOException;

import org.rascalmpl.interpreter.IEvaluatorContext;  // TODO: remove import? NOT YET: Only used as argument of reflective library function
import org.rascalmpl.value.IBool;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.type.TypeStore;


public class ExecuteProgram {
	IValueFactory vf;
	

	public ExecuteProgram(IValueFactory vf) {
		this.vf =vf;
	}
	
	// Library function to serialize a RVMProgram

	public void linkAndSerializeProgram(
			ISourceLocation rvmProgramLoc,
			IConstructor rvmProgram,
			IBool jvm
			) throws IOException {

	    TypeStore typeStore = new TypeStore();
		RVMExecutable exec = ExecutionTools.link(rvmProgram, jvm, typeStore);
		exec.write(rvmProgramLoc);
	}
	
	// Library function to execute a RVMProgram
	// (Interpreter version)

	public IValue executeProgram(
			IConstructor rvmProgram,
			IMap keywordArguments,
			IBool debug,
			IBool debugRVM, 
			IBool testsuite, 
			IBool profile, 
			IBool trace, 
			IBool coverage, 
			IBool jvm,
			IEvaluatorContext ctx
			) throws IOException {
		
	    TypeStore typeStore = new TypeStore();
		RVMExecutable executable = ExecutionTools.link(rvmProgram, jvm, typeStore);
		if(executable.isValid()){
			RascalExecutionContext rex = ExecutionTools.makeRex(null /* kernel not needed in interpreter version */, executable, ctx.getStdOut(), ctx.getStdErr(), debug, debugRVM, testsuite, profile, trace, coverage, jvm, ctx.getEvaluator().getRascalResolver(), typeStore);
			
			// to be able to link the classes necessary for compiling parsers and to link builtins implemented based on jars
			for (ClassLoader l : ctx.getEvaluator().getClassLoaders()) {
			    rex.addClassLoader(l);
			}
			
			rex.getConfiguration().setRascalJavaClassPathProperty(ctx.getConfiguration().getRascalJavaClassPathProperty());
			
			return ExecutionTools.executeProgram(executable, new KWArgs(vf).add(keywordArguments).build(), rex);
		} else {
			throw new IOException("Cannot execute program with errors: " + executable.getErrors().toString());
		}
	}

	// Library function to execute an RVMProgram
	// (Compiler version)

	public IValue executeProgram(
			IConstructor rvmProgram,
			IMap keywordArguments,
			IBool debug,
			IBool debugRVM, 
			IBool testsuite, 
			IBool profile, 
			IBool trace, 
			IBool coverage, 
			IBool jvm,
			RascalExecutionContext rex
			) throws IOException {

	    TypeStore typeStore = new TypeStore();
		RVMExecutable executable = ExecutionTools.link(rvmProgram, jvm, typeStore);

		if(executable.isValid()){
			RascalExecutionContext rex2 = ExecutionTools.makeRex(rex.getBoot(), executable, rex.getStdOut(), rex.getStdErr(), debug, debugRVM, testsuite, profile, trace, coverage, jvm, rex.getRascalSearchPath(), typeStore);
			
			return ExecutionTools.executeProgram(executable, new KWArgs(vf).add(keywordArguments).build(), rex2);
		} else {
			throw new IOException("Cannot execute program with errors: " + executable.getErrors().toString());
		}
	}

	// Library function to link and execute a RVM program from file
	// (Interpreter version)

	public IValue executeProgram(
			ISourceLocation rvmExecutableLoc,
			IMap keywordArguments,
			IBool debug,
			IBool debugRVM, 
			IBool testsuite, 
			IBool profile, 
			IBool trace, 
			IBool coverage,
			IBool jvm,
			IEvaluatorContext ctx
			) throws IOException {
       
	    TypeStore typeStore = new TypeStore();
        
		RVMExecutable executable = ExecutionTools.load(rvmExecutableLoc, typeStore);
		if(executable.isValid()){
	       RascalExecutionContext rex = ExecutionTools.makeRex(null /* kernel not needed in interpreter version */, executable, ctx.getStdOut(), ctx.getStdErr(), debug, debugRVM, testsuite, profile, trace, coverage, jvm, ctx.getEvaluator().getRascalResolver(), typeStore);
			// to be able to link the classes necessary for compiling parsers and to link builtins implemented based on jars
            for (ClassLoader l : ctx.getEvaluator().getClassLoaders()) {
                rex.addClassLoader(l);
            }
            
            rex.getConfiguration().setRascalJavaClassPathProperty(ctx.getConfiguration().getRascalJavaClassPathProperty());
            
			return ExecutionTools.executeProgram(executable, new KWArgs(vf).add(keywordArguments).build(), rex);
		} else {
			throw new IOException("Cannot execute program with errors: " + executable.getErrors().toString());
		}
	}
		
	// Library function to link and execute a RVM program from file
	// (Compiler version)

	public IValue executeProgram(
			ISourceLocation rvmExecutableLoc,
			IMap keywordArguments,
			IBool debug,
			IBool debugRVM, 
			IBool testsuite, 
			IBool profile, 
			IBool trace, 
			IBool coverage,
			IBool jvm,
			RascalExecutionContext rex
			) throws IOException {
	    TypeStore typeStore = new TypeStore();
		RVMExecutable executable = ExecutionTools.load(rvmExecutableLoc, typeStore);
		if(executable.isValid()){
			RascalExecutionContext rex2 = ExecutionTools.makeRex(rex.getBoot(), executable, rex.getStdOut(), rex.getStdErr(), debug, debugRVM, testsuite, profile, trace, coverage, jvm, rex.getRascalSearchPath(), typeStore);
			
			return ExecutionTools.executeProgram(executable, new KWArgs(vf).add(keywordArguments).build(), rex2);
		} else {
			throw new IOException("Cannot execute program with errors: " + executable.getErrors().toString());
		}
	}

}
