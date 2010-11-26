package org.erlide.core.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.erlide.jinterface.backend.RuntimeVersion;

import com.google.common.collect.Sets;

public class ErlProjectInfo {

    private final RuntimeVersion requiredRuntimeVersion;
    private final ErlProjectLayout layout;

    private final Set<PathEntry> codePathEntries = Sets.newHashSet();

    private final Set<PathEntry> dependencies = Sets.newHashSet();

    public ErlProjectInfo() {
        this(ErlProjectLayout.OTP_LAYOUT);
    }

    public ErlProjectInfo(final ErlProjectLayout layout) {
        this(new RuntimeVersion("R14B"), layout);
    }

    public ErlProjectInfo(final RuntimeVersion version,
            final ErlProjectLayout layout) {
        requiredRuntimeVersion = version;
        this.layout = layout;
    }

    public Collection<PathEntry> getDependencies() {
        return Collections.unmodifiableCollection(dependencies);
    }

    public RuntimeVersion getRequiredRuntimeVersion() {
        return requiredRuntimeVersion;
    }

    public ErlProjectLayout getLayout() {
        return layout;
    }

    public ErlProjectInfo addDependencies(final Collection<PathEntry> locations) {
        final Collection<PathEntry> dependencies = getDependencies();
        for (final PathEntry loc : locations) {
            if (!dependencies.contains(loc)) {
                dependencies.add(loc);
            }
        }
        return new ErlProjectInfo(/* dependencies */);
    }

    public ErlProjectInfo removeDependencies(
            final Collection<PathEntry> locations) {
        final Collection<PathEntry> dependencies = getDependencies();
        for (final PathEntry loc : locations) {
            dependencies.remove(loc);
        }
        return new ErlProjectInfo(/* dependencies */);
    }

    public ErlProjectInfo setRequiredRuntimeVersion(
            final RuntimeVersion runtimeVersion) {
        return new ErlProjectInfo(/* runtimeVersion */);
    }

}
