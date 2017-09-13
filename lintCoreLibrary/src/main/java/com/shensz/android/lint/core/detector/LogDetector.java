package com.shensz.android.lint.core.detector;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;

/**
 * 提醒不要使用Log或者System.out.println
 * Created by Tao on 17/9/11.
 */

public class LogDetector extends Detector implements Detector.JavaScanner{
    public static final Issue ISSUE = Issue.create(
            "LogUse",
            "避免使用Log/System.out.println",
            "防止在正式包打印log",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(LogDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.<Class<? extends Node>>singletonList(MethodInvocation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(final JavaContext context) {
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitMethodInvocation(MethodInvocation node) {
                JavaParser.ResolvedNode resolve = context.resolve(node);
                if (resolve instanceof JavaParser.ResolvedMethod) {
                    JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolve;
                    // 方法所在的类校验
                    JavaParser.ResolvedClass containingClass = method.getContainingClass();
                    if (containingClass.matches("android.util.Log")) {
                        context.report(ISSUE, node, context.getLocation(node),
                                "为了防止在正式包打印log，请不要直接使用Log");
                        return true;
                    }
                    if (node.toString().startsWith("System.out.print")) {
                        context.report(ISSUE, node, context.getLocation(node),
                                "为了防止在正式包打印log，请不要直接使用System.out.print");
                        return true;
                    }
                }
                return super.visitMethodInvocation(node);
            }
        };
    }
}
