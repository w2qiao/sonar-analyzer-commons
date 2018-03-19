/*
 * SonarQube Analyzer Commons
 * Copyright (C) 2009-2018 SonarSource SA
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
package org.sonarsource.analyzer.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Not designed for multi-threads
 */
class JsonParser {
  private Method caller;
  private Object obj;

  JsonParser() {
    try {
      obj = new ScriptEngineManager().getEngineByName("nashorn").eval("JSON.parse");
      // obj is of type JSObject, but was potentially created by a different classloader, so it's risky to assign it to a JSObject.
      caller = obj.getClass().getMethod("call", Object.class, Object[].class);
    } catch (ScriptException | NoSuchMethodException e) {
      throw new IllegalStateException("Can not get 'JSON.parse' from 'nashorn' script engine.", e);
    }
  }

  Map<String, Object> parse(String data) {
    try {
      return (Map<String, Object>) caller.invoke(obj, null, new Object[] {data});
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Failed to invoke 'nashorn' script engine.", e);
    }
  }

}
