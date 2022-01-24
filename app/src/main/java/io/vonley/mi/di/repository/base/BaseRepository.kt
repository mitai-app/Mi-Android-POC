package io.vonley.mi.di.repository.base

abstract class BaseRepository<T>(val dao: T) : Repository<T> {
}