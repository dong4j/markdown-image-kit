package info.dong4j.idea.plugin.sdk.qcloud.cos.model.Tag;

import info.dong4j.idea.plugin.sdk.qcloud.cos.model.lifecycle.LifecycleFilterPredicate;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.lifecycle.LifecyclePredicateVisitor;

/**
 * A {@link LifecycleFilterPredicate} class to represent the {@link Tag} object
 * that must exist in the object's tag set in order for the
 * {@link info.dong4j.idea.plugin.sdk.qcloud.cos.modle.BucketLifecycleConfiguration.Rule} to apply.
 */
public final class LifecycleTagPredicate extends LifecycleFilterPredicate {

    private final Tag tag;

    public LifecycleTagPredicate(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public void accept(LifecyclePredicateVisitor lifecyclePredicateVisitor) {
        lifecyclePredicateVisitor.visit(this);
    }
}