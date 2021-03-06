/*
 * Copyright (c) 2000
 *      Jon Schewe.  All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
 */
package net.mtu.eggplant.dbc;

/**
 * Used for representing a modification to code, rather than just a chunck of
 * code to be inserted.
 *
 * @version $Revision: 1.6 $
 */
public class CodeModification extends CodeFragment {

  /**
     @param start the start of the region to modify
     @param searchText what to find in the region
     @param replaceText what to replace search with
     @param type the type of assertion this is

     @pre (start != null)
     @pre (searchText != null)
     @pre (replaceText != null)
  **/
  public CodeModification(final CodePoint start,
                          final String searchText,
                          final String replaceText,
                          final CodeFragmentType type) {
    super(start, replaceText, type);
    _searchText = searchText;
  }

  public final String getSearchText() {
    return _searchText;
  }
  private String _searchText;
  
  public final String getReplaceText() {
    return getCode();
  }

  public int instrumentLine(final int offset,
                            final StringBuffer line) {
    final int start = getLocation().getColumn() + offset;
    final String search = getSearchText();
    final String replace = getReplaceText();
    final int newOffset = offset + (replace.length() - search.length());
    final int searchTextIndex = line.toString().indexOf(search, start);
    line.replace(start, start+search.length(), replace);

    return newOffset;
  }
}
