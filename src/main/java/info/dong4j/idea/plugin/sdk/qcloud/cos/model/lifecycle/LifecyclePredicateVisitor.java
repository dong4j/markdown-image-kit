package info.dong4j.idea.plugin.sdk.qcloud.cos.model.lifecycle;

import info.dong4j.idea.plugin.sdk.qcloud.cos.model.Tag.LifecycleTagPredicate;

/**
 * Interface to invoke specific behavior based on the type of {@link LifecycleFilterPredicate} visited.
 * This follows the visitor design pattern.
 *
 * When an implementation of this visitor is passed to an
 * {@link LifecycleFilterPredicate#accept(LifecyclePredicateVisitor)} method,
 * the visit method most applicable to that element is invoked.
 */
public interface LifecyclePredicateVisitor {

    /**
     * Implement this method to add behaviour performed when
     * {@link LifecyclePrefixPredicate} is visited.
     */
    void visit(LifecyclePrefixPredicate lifecyclePrefixPredicate);

    /**
     * Implement this method to add behaviour performed when
     * {@link LifecycleTagPredicate} is visited.
     */
    void visit(LifecycleTagPredicate lifecycleTagPredicate);

    /**
     * Implement this method to add behaviour performed when
     * {@link LifecycleAndOperator} is visited.
     */
    void visit(LifecycleAndOperator lifecycleAndOperator);
}
