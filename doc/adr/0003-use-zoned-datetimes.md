# 0003 - Use Zoned Datetimes

2022-08-09

## Status

Accepted

## Context

Services and data stores across HMPPS have been developed over a long period
of time. Different services have made different decisions about how to store
and transmit date and time information for the entities they are responsible
for. Dates and times are often stored without timezones in specific services,
and these services rely on the timezone of the base server when displaying or
transmitting dates and times. Whilst HMPPS services were self-contained this
did not matter much, as any dates and times were processed consistently by a
single service. Now that we are expanding the HMPPS Digital environment and
transfer date and time information across multiple services, store them in a
number of places and use the information to indicate when activity happened in
a different service, transmitting dates and times without a timezone causes
issues.

The Delius database does not store times with a timezone value, but relies on
the timezone of the underlying server to determine the timezone of date/time
fields.

Our use of containers for HMPPS services means that it is possible for the
configuration of a container image (possibly a base image) to affect the
timezone of a service. This makes timezone issues much more likely to happen
than when running on a bare-metal server configuration

## Decision

- For new interactions we will require all incoming date/time fields to have
  an associated timezone
- Internally when we process date/times within the Probation Integration
  Services codebase we will use `java.time.ZonedDateTime` objects

## Consequences

Requiring a timezone on date/time fields means that we have a specific and
absolute point in time that we can store within the Delius database. When we
store date/time information in the Delius database we lose the timezone
information, however, by requiring the timezone on the incoming information we
can at least determine the correct point in time when storing the field, based
on the timezone of the underlying Delius server. This should enable us to
reduce 'off-by-one-hour' bugs when bringing data in from other services.

While these kind of timezone issues will only be fully mitigated by storing
the timezone with date/time fields, which for older services like Delius may
not be possible, we can at least provide complete information on the source
timezone of date/time fields in case this does become possible in the future
