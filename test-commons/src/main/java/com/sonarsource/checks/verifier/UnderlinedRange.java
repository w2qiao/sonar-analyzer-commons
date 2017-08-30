/*
 * SonarQube Analyzer Test Commons
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonarsource.checks.verifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to specify the location of an issue in source code.
 */
public class UnderlinedRange implements Comparable<UnderlinedRange> {

  /**
   * start at 1, line number of the first character of the token
   */
  public final int line;

  /**
   * start at 1, column number of the first character of the token
   */
  public final int column;

  /**
   * start at 1, line number of the last character of the token
   * if the token is on a single line, endLine == line
   */
  public final int endLine;

  /**
   * start at 1, column number of the last character of the token
   * if the token has only one character, endColumn == column
   */
  public final int endColumn;

  public UnderlinedRange(int line, int column, int endLine, int endColumn) {
    this.line = line;
    this.column = column;
    this.endLine = endLine;
    this.endColumn = endColumn;
    if (endLine < line || (endLine == line && endColumn < column)) {
      throw new IndexOutOfBoundsException(toString());
    }
    if (line < 1 || column < 1 || (endLine != line && endColumn < 1)) {
      throw new IndexOutOfBoundsException(toString());
    }
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof UnderlinedRange && this.compareTo((UnderlinedRange) other) == 0;
  }

  @Override
  public int hashCode() {
    return 31 * (31 * (31 * line + column) + endLine) + endColumn;
  }

  @Override
  public int compareTo(UnderlinedRange other) {
    int diff = Integer.compare(line, other.line);
    if (diff != 0) {
      return diff;
    }
    diff = Integer.compare(column, other.column);
    if (diff != 0) {
      return diff;
    }
    diff = Integer.compare(endLine, other.endLine);
    if (diff != 0) {
      return diff;
    }
    return Integer.compare(endColumn, other.endColumn);
  }

  @Override
  public String toString() {
    return "(" + line + ":" + column + "," + endLine + ":" + endColumn + ")";
  }

  public void underline(int indent, StringBuilder textLine) {
    for (int i = textLine.length(); i < (indent + column) - 1; i++) {
      textLine.append(' ');
    }
    if (textLine.length() > 0 && textLine.charAt(textLine.length() - 1) == '^') {
      textLine.append(' ');
    }
    if (textLine.length() >= (indent + column) || endLine != line) {
      List<String> params = new ArrayList<>();
      if (textLine.length() >= (indent + column)) {
        params.add("sc=" + column);
      }
      if (endLine > line) {
        params.add("el=+" + (endLine - line));
      } else if (endLine < line) {
        params.add("el=-" + (line - endLine));
      }
      params.add("ec=" + endColumn);
      textLine.append("^[");
      textLine.append(params.stream().collect(Collectors.joining(";")));
      textLine.append("]");
    } else {
      while (textLine.length() < (indent + endColumn)) {
        textLine.append('^');
      }
    }
  }

}
