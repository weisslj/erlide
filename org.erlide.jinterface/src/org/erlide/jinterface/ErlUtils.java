/*******************************************************************************
 * Copyright (c) 2004 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.jinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.erlide.jinterface.rpc.RpcConverter;
import org.erlide.jinterface.rpc.RpcException;
import org.erlide.jinterface.rpc.Signature;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpFormatPlaceholder;
import com.ericsson.otp.erlang.OtpPatternVariable;

public class ErlUtils {

	private ErlUtils() {
	}

	public static OtpErlangObject parse(String string) throws ParserException {
		return TermParser.parse(string);
	}

	/**
	 * Build an Erlang (extended) term from a textual description. For example,
	 * <code> format("{hello, ~s, [~a, _]}", "myname", "mykey")
	 * </code> gives the equivalent of <code> {hello, "myname", [mykey, _]}
	 * </code>.
	 * <p>
	 * Items beginning with ~ are placeholders that will be replaced with the
	 * corresponding argument (from left to right). The text after the ~ is the
	 * type signature of the argument, so that automatic conversion Java->Erlang
	 * can be done. See RpcConverter.java2erlang for details.
	 * 
	 * @throws RpcException
	 * @see org.erlide.jinterface.rpc.RpcConverter
	 */
	public static OtpErlangObject format(String fmt, Object... args)
			throws ParserException, RpcException {
		OtpErlangObject result;
		result = parse(fmt);
		List<Object> values = new ArrayList<Object>(args.length);
		values = new ArrayList<Object>(Arrays.asList(args));
		result = fill(result, values);
		return result;
	}

	public static Bindings match(String pattern, String term)
			throws ParserException {
		return match(parse(pattern), parse(term), new Bindings());
	}

	public static Bindings match(String pattern, OtpErlangObject term)
			throws ParserException {
		return match(parse(pattern), term, new Bindings());
	}

	public static Bindings match(String pattern, OtpErlangObject term,
			Bindings bindings) throws ParserException {
		return match(parse(pattern), term, bindings);
	}

	public static Bindings match(String pattern, String term, Bindings bindings)
			throws ParserException {
		return match(parse(pattern), parse(term), bindings);
	}

	public static Bindings match(OtpErlangObject pattern, OtpErlangObject term) {
		return match(pattern, term, new Bindings());
	}

	public static Bindings match(OtpErlangObject pattern, String term)
			throws ParserException {
		return match(pattern, parse(term), new Bindings());
	}

	/**
	 * Match two Erlang terms.
	 * <p>
	 * Patterns have an extended syntax:
	 * <ul>
	 * <li>Variables can have a type signature attached, like for example
	 * <code>Var:i</code>. Its meaning is that the type of the value must match
	 * too.</li>
	 * <li>The tail of a list can only be a variable.</li>
	 * </ul>
	 * <p>
	 * The returned value is null if there was any mismatch, otherwise it is a
	 * map of variable names to matched values. <br>
	 * TODO should we throw an exception instead?
	 * 
	 * @throws RpcException
	 */
	public static Bindings match(OtpErlangObject pattern, OtpErlangObject term,
			Bindings bindings) {
		if (pattern == null && term == null) {
			return bindings;
		}
		if (pattern == null || term == null) {
			return null;
		}
		if (pattern instanceof OtpPatternVariable) {
			OtpPatternVariable var = (OtpPatternVariable) pattern;
			if (!RpcConverter.matchSignature(term, var.getSignature())) {
				return null;
			}
			if (var.getName().equals("_")) {
				return bindings;
			}
			final Bindings result = new Bindings(bindings);
			final OtpErlangObject old = bindings.get(var.getName());
			if (old == null) {
				// no previous binding
				result.put(var.getName(), term);
				return result;
			} else {
				return old.equals(term) ? result : null;
			}
		}
		if (!pattern.getClass().equals(term.getClass())) {
			return null;
		}

		if (pattern.equals(term)) {
			return bindings;
		} else if (pattern instanceof OtpErlangList) {
			return matchList(pattern, term, bindings);
		} else if (pattern instanceof OtpErlangTuple) {
			return matchTuple(((OtpErlangTuple) pattern).elements(),
					((OtpErlangTuple) term).elements(), bindings, false);
		}
		return null;
	}

	private static Bindings matchList(OtpErlangObject pattern,
			OtpErlangObject term, Bindings bindings) {
		OtpErlangList lpattern = (OtpErlangList) pattern;
		OtpErlangList lterm = (OtpErlangList) term;
		final int patternArity = lpattern.arity();
		final int termArity = lterm.arity();
		if (patternArity > termArity) {
			return null;
		}
		if (patternArity < termArity && lpattern.isProper()) {
			return null;
		}
		if (patternArity == termArity
				&& lpattern.isProper() != lterm.isProper()) {
			return null;
		}
		Bindings rez = bindings;
		for (int i = 0; i < patternArity; i++) {
			rez = match(lpattern.elementAt(i), lterm.elementAt(i), rez);
			if (rez == null) {
				return null;
			}
		}
		if (patternArity == termArity) {
			rez = match(lpattern.getLastTail(), lterm.getLastTail(), rez);
			return rez;
		}
		if (lpattern.getLastTail() instanceof OtpPatternVariable) {
			return match(lpattern.getLastTail(),
					lterm.getNthTail(patternArity), rez);
		}
		return match(lpattern.getLastTail(), lterm.getLastTail(), rez);
	}

	private static OtpErlangObject fill(OtpErlangObject template,
			List<Object> values) throws RpcException, ParserException {
		if (values.size() == 0) {
			return template;
		}
		if (template instanceof OtpErlangList) {
			OtpErlangObject[] elements = ((OtpErlangList) template).elements();
			List<OtpErlangObject> result = new ArrayList<OtpErlangObject>(
					elements.length);
			for (OtpErlangObject elem : elements) {
				result.add(fill(elem, values));
			}
			return new OtpErlangList(result.toArray(elements));
		} else if (template instanceof OtpErlangTuple) {
			OtpErlangObject[] elements = ((OtpErlangTuple) template).elements();
			List<OtpErlangObject> result = new ArrayList<OtpErlangObject>(
					elements.length);
			for (OtpErlangObject elem : elements) {
				result.add(fill(elem, values));
			}
			return new OtpErlangTuple(result.toArray(elements));
		} else if (template instanceof OtpFormatPlaceholder) {
			OtpFormatPlaceholder holder = (OtpFormatPlaceholder) template;
			Object ret = values.remove(0);
			Signature[] signs = Signature.parse(holder.getName());
			if (signs.length == 0 && !(ret instanceof OtpErlangObject)) {
				throw new ParserException("funny placeholder");
			}
			Signature sign = (signs.length == 0) ? new Signature('x')
					: signs[0];
			return RpcConverter.java2erlang(ret, sign);
		} else {
			return template;
		}
	}

	private static Bindings matchTuple(OtpErlangObject[] patterns,
			OtpErlangObject[] terms, Bindings bindings, boolean list) {
		Bindings result = new Bindings(bindings);
		for (int i = 0; i < patterns.length; i++) {
			result = match(patterns[i], terms[i], result);
			if (result == null) {
				return null;
			}
		}
		return result;
	}
}
