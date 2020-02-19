package uk.ac.cam.gp.charlie.ast;

/**
 * Represents the value an attribute is assigned in a Thing,
 * at the moment this is either:
 * - a Variable
 *  e.g. '$x' in '$y has name $x'
 * - a ConstantValue
 *  e.g. 'Bob' in '$y has name "Bob"'
 */
public abstract class AttributeValue {

}
