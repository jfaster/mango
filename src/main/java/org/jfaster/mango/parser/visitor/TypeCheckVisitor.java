/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.parser.visitor;

import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.parser.ParserVisitor;
import org.jfaster.mango.parser.node.*;
import org.jfaster.mango.support.TypeContext;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 类型检测
 *
 * @author ash
 */
public class TypeCheckVisitor implements ParserVisitor {

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ASTRootNode node, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTJDBCParameter node, Object data) {
        TypeContext context = getTypeContext(data);
        Type type = context.getPropertyType(node.getParameterName(), node.getPropertyPath());
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null || typeToken.isIterable() || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new IncorrectParameterTypeException("invalid type of " + node.getFullName() + ", " +
                    "expected a class can be identified by jdbc but " + type);
        }
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTJDBCIterableParameter node, Object data) {
        TypeContext context = getTypeContext(data);
        Type type = context.getPropertyType(node.getParameterName(), node.getPropertyPath());
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (!typeToken.isIterable()) { // 不是集合或数组抛出异常
            throw new IncorrectParameterTypeException("invalid type of " + node.getFullName() + ", " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + type);
        }
        if (mappedClass == null || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            String s = typeToken.isArray() ? "component" : "actual";
            throw new IncorrectParameterTypeException("invalid " + s + " type of " + node.getFullName() + ", " +
                    s + " type of " + node.getFullName() + " expected a class can be identified by jdbc " +
                    "but " + typeToken.getMappedType());
        }
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGlobalTable node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTJoinParameter node, Object data) {
        TypeContext context = getTypeContext(data);
        context.getPropertyType(node.getParameterName(), node.getPropertyPath()); // 保证能取到即可
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTText node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTBlank node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTElseStatement node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTElseIfStatement node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTOrNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTAndNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTLTNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGTNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTLENode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGENode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTEQNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTNENode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTNotNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTExpressionParameter node, Object data) {
        TypeContext context = getTypeContext(data);
        context.getPropertyType(node.getParameterName(), node.getPropertyPath()); // 保证能取到即可
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTIntegerLiteral node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTTrue node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTFalse node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTNull node, Object data) {
        return node.childrenAccept(this, data);
    }

    private TypeContext getTypeContext(Object data) {
        return (TypeContext) data;
    }

}
