/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

/**
   Used for representing a modification to code, rather than just a chunck of
   code to be inserted.

**/
public class CodeModification extends CodeFragment {

  /**
     @param start the start of the region to modify
     @param end the end of the region to modify
     @param search what to find in the region
     @param replace what to replace search with
     @param code what to insert at the end of the region, after end
     @param type the type of assertion this is

     @pre (start != null)
     @pre (end != null)
     @pre (search != null)
     @pre (replace != null)
     @pre (code != null)
  **/
  public CodeModification(final CodePoint start,
                          final String searchText,
                          final String replaceText,
                          final CodeFragmentType type) {
    super(start, replaceText, type);
    _searchText = searchText;
  }

  final public String getSearchText() {
    return _searchText;
  }
  private String _searchText;
  
  final public String getReplaceText() {
    return getCode();
  }

  public int instrumentLine(final int offset,
                            final StringBuffer line) {
    int start = getLocation().getColumn() + offset;
    String search = getSearchText();
    String replace = getReplaceText();
    int newOffset = offset + (replace.length() - search.length());
    int searchTextIndex = line.toString().indexOf(getSearchText(), start);
    line.replace(start, start+search.length(), replace);

    return newOffset;
  }
}
