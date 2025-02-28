/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.indices.replication.checkpoint;

import org.opensearch.common.Nullable;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;
import org.opensearch.index.seqno.SequenceNumbers;
import org.opensearch.index.shard.ShardId;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a Replication Checkpoint which is sent to a replica shard.
 *
 * @opensearch.internal
 */
public class ReplicationCheckpoint implements Writeable, Comparable<ReplicationCheckpoint> {

    private final ShardId shardId;
    private final long primaryTerm;
    private final long segmentsGen;
    private final long segmentInfosVersion;
    private final long length;

    public static ReplicationCheckpoint empty(ShardId shardId) {
        return new ReplicationCheckpoint(shardId);
    }

    private ReplicationCheckpoint(ShardId shardId) {
        this.shardId = shardId;
        primaryTerm = SequenceNumbers.UNASSIGNED_PRIMARY_TERM;
        segmentsGen = SequenceNumbers.NO_OPS_PERFORMED;
        segmentInfosVersion = SequenceNumbers.NO_OPS_PERFORMED;
        length = 0L;
    }

    public ReplicationCheckpoint(ShardId shardId, long primaryTerm, long segmentsGen, long segmentInfosVersion) {
        this(shardId, primaryTerm, segmentsGen, segmentInfosVersion, 0L);
    }

    public ReplicationCheckpoint(ShardId shardId, long primaryTerm, long segmentsGen, long segmentInfosVersion, long length) {
        this.shardId = shardId;
        this.primaryTerm = primaryTerm;
        this.segmentsGen = segmentsGen;
        this.segmentInfosVersion = segmentInfosVersion;
        this.length = length;
    }

    public ReplicationCheckpoint(StreamInput in) throws IOException {
        shardId = new ShardId(in);
        primaryTerm = in.readLong();
        segmentsGen = in.readLong();
        segmentInfosVersion = in.readLong();
        length = in.readLong();
    }

    /**
     * The primary term of this Replication Checkpoint.
     *
     * @return the primary term
     */
    public long getPrimaryTerm() {
        return primaryTerm;
    }

    /**
     * @return the Segments Gen number
     */
    public long getSegmentsGen() {
        return segmentsGen;
    }

    /**
     * @return the Segment Info version
     */
    public long getSegmentInfosVersion() {
        return segmentInfosVersion;
    }

    /**
     * Shard Id of primary shard.
     *
     * @return the Shard Id
     */
    public ShardId getShardId() {
        return shardId;
    }

    /**
     * @return The size in bytes of this checkpoint.
     */
    public long getLength() {
        return length;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        shardId.writeTo(out);
        out.writeLong(primaryTerm);
        out.writeLong(segmentsGen);
        out.writeLong(segmentInfosVersion);
        out.writeLong(length);
    }

    @Override
    public int compareTo(ReplicationCheckpoint other) {
        return this.isAheadOf(other) ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplicationCheckpoint that = (ReplicationCheckpoint) o;
        return primaryTerm == that.primaryTerm
            && segmentsGen == that.segmentsGen
            && segmentInfosVersion == that.segmentInfosVersion
            && Objects.equals(shardId, that.shardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, primaryTerm, segmentsGen);
    }

    /**
     * Checks if current replication checkpoint is AheadOf `other` replication checkpoint point by first comparing
     * primaryTerm followed by segmentInfosVersion. Returns true when `other` is null.
     */
    public boolean isAheadOf(@Nullable ReplicationCheckpoint other) {
        return other == null
            || primaryTerm > other.getPrimaryTerm()
            || (primaryTerm == other.getPrimaryTerm() && segmentInfosVersion > other.getSegmentInfosVersion());
    }

    @Override
    public String toString() {
        return "ReplicationCheckpoint{"
            + "shardId="
            + shardId
            + ", primaryTerm="
            + primaryTerm
            + ", segmentsGen="
            + segmentsGen
            + ", version="
            + segmentInfosVersion
            + ", size="
            + length
            + '}';
    }
}
