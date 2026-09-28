package com.linbit.linstor.layer.storage.spdk;

import com.linbit.linstor.storage.StorageException;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public interface SpdkCommands<T>
{
    T createFat(
        String volumeGroup,
        String vlmId,
        long size,
        String... additionalParameters
    )
        throws StorageException;

    T resize(String volumeGroupRef, String vlmIdRef, long sizeRef) throws StorageException;

    T rename(String volumeGroupRef, String vlmCurrentIdRef, String vlmNewIdRef)
        throws StorageException;

    T delete(String volumeGroupRef, String vlmIdRef) throws StorageException;

    void ensureTransportExists(String typeRef) throws StorageException;

    T lvs() throws StorageException;

    T getLvolStores() throws StorageException;

    T lvsByName(String nameRef) throws StorageException;

    T getNvmfSubsystems() throws StorageException;

    T nvmSubsystemCreate(String subsystemNameRef) throws StorageException;

    T nvmfSubsystemAddListener(
        String subsystemNameRef,
        String transportTypeRef,
        String addressRef,
        String stringRef,
        String portRef
    )
        throws StorageException;

    T nvmfSubsystemAddNs(String subsystemNameRef, String stringRef) throws StorageException;

    T nvmfDeleteSubsystem(String subsystemNameRef) throws StorageException;

    T nvmfSubsystemRemoveNamespace(String subsystemNameRef, int namespaceNrRef)
        throws StorageException;

    Iterator<JsonNode> getJsonElements(T data) throws StorageException;

    T createSnapshot(String fullQualifiedVlmId, String snapName) throws StorageException;

    T restoreSnapshot(String fullQualifiedSnapId, String newVlmId)
        throws StorageException;

    T decoupleParent(String fullQualifiedIdentifierRef) throws StorageException;

    T clone(String fullQualifiedSourceSnapNameRef, String lvTargetIdRef)
        throws StorageException;
}
