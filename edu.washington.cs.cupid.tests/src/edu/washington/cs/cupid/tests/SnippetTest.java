package edu.washington.cs.cupid.tests;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.snippet.SnippetCapability;

public class SnippetTest {

	public static SnippetCapability<Object, Boolean> RETURNS_TRUE = 
			new SnippetCapability<Object,Boolean>(
					"True Snippet", "edu.washington.cs.cupid.tests.snippet.true",
					"A snippet that returns true",
					TypeToken.of(Object.class), TypeToken.of(Boolean.class),
					"true");
}
