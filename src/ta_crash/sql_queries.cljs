(ns ta-crash.sql-queries
  (:require-macros [sqlize.core :refer [def-sql-query]]))

(def-sql-query "-- name: distinct-borough
  -- Selects distinct boroughs
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_borough")

(def-sql-query "-- name: distinct-city-council
  -- Selects distinct city council districts
  SELECT DISTINCT
    identifier
  FROM
    nyc_city_council")

(def-sql-query "-- name: distinct-community-board
  -- Selects distinct community boards
  SELECT DISTINCT
    identifier
  FROM
    nyc_community_board")

(def-sql-query "-- name: distinct-neighborhood
  -- Selects distinct neighborhoods
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_neighborhood")

(def-sql-query "-- name: distinct-precinct
  -- Selects distinct nypd precincts
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_nypd_precinct")

(def-sql-query "-- name: distinct-zip-code
  -- Selects distinct zip codes
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_zip_codes")

(def-sql-query "-- name: all-factors-date
  -- Counts all factors for a given date range.
  WITH all_factors as (
    SELECT
      contributing_factor_vehicle_1 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    UNION ALL
    SELECT
      contributing_factor_vehicle_2 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      contributing_factor_vehicle_3 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      contributing_factor_vehicle_4 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      contributing_factor_vehicle_5 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
  )
  SELECT
    count(af.factor) as count_factor,
    af.factor
  FROM
    all_factors af
  GROUP BY
    af.factor
  ORDER BY
    count_factor desc")

(def-sql-query "-- name: all-vehicle-types-date
  -- Counts all vehicle types for a given date range.
  WITH all_vehicle_types as (
    SELECT
      vehicle_type_code_1 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    UNION ALL
    SELECT
      vehicle_type_code_2 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      vehicle_type_code_3 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      vehicle_type_code_4 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      vehicle_type_code_5 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
  )
  select
    count(avt.vehicle_type) as count_vehicle_type,
    avt.vehicle_type
  from
    all_vehicle_types avt
  group by
    avt.vehicle_type
  order by
    count_vehicle_type desc")

(def-sql-query "-- name: stats-date
  -- Counts all death / injury stats for a given date range
  SELECT
    count(c.cartodb_id) as total_crashes,
    (select count(cartodb_id) from table_20k_crashes where (number_of_cyclist_injured > 0) or (number_of_motorist_injured > 0) or (number_of_pedestrians_injured > 0) or (number_of_persons_injured > 0)) as total_crashes_with_injury,
    (select count(cartodb_id) from table_20k_crashes where (number_of_cyclist_killed > 0) or (number_of_motorist_killed > 0) or (number_of_pedestrians_killed > 0) or (number_of_persons_killed > 0)) as total_crashes_with_death,
    sum(c.number_of_cyclist_injured) as cyclist_injured,
    sum(c.number_of_cyclist_killed) as cyclist_killed,
    sum(c.number_of_motorist_injured) as motorist_injured,
    sum(c.number_of_motorist_killed) as motorist_killed,
    sum(c.number_of_pedestrians_injured) as pedestrians_injured,
    sum(c.number_of_pedestrians_killed) as pedestrians_killed,
    sum(c.number_of_persons_injured) as persons_injured,
    sum(c.number_of_persons_killed) as persons_killed
  FROM
    table_20k_crashes c
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')")

(def-sql-query "-- name: crashes-for-factor-date
  -- Selects crashes for a given date range & factor
  SELECT
    c.cartodb_id,
    c.borough,
    c.contributing_factor_vehicle_1,
    c.contributing_factor_vehicle_2,
    c.contributing_factor_vehicle_3,
    c.contributing_factor_vehicle_4,
    c.contributing_factor_vehicle_5,
    c.cross_street_name,
    c.off_street_name,
    c.on_street_name,
    c.zip_code,
    c.date,
    c.time,
    c.vehicle_type_code_1,
    c.vehicle_type_code_2,
    c.vehicle_type_code_3,
    c.vehicle_type_code_4,
    c.vehicle_type_code_5,
    c.number_of_cyclist_injured,
    c.number_of_cyclist_killed,
    c.number_of_motorist_injured,
    c.number_of_motorist_killed,
    c.number_of_pedestrians_injured,
    c.number_of_pedestrians_killed,
    c.number_of_persons_injured,
    c.number_of_persons_killed
  FROM
    table_20k_crashes c
  WHERE
    ((contributing_factor_vehicle_1 = ':factor')
    OR
     (contributing_factor_vehicle_2 = ':factor')
    OR
     (contributing_factor_vehicle_3 = ':factor')
    OR
     (contributing_factor_vehicle_4 = ':factor')
    OR
     (contributing_factor_vehicle_5 = ':factor'))
  AND
    ((date <= date ':end-date')
    AND
     (date >= date ':start-date'))")

(def-sql-query "-- name: crashes-for-vehicle-date
  -- Selects crashes for a given date range & vehicle type
  SELECT
    c.cartodb_id,
    c.borough,
    c.contributing_factor_vehicle_1,
    c.contributing_factor_vehicle_2,
    c.contributing_factor_vehicle_3,
    c.contributing_factor_vehicle_4,
    c.contributing_factor_vehicle_5,
    c.cross_street_name,
    c.off_street_name,
    c.on_street_name,
    c.zip_code,
    c.date,
    c.time,
    c.vehicle_type_code_1,
    c.vehicle_type_code_2,
    c.vehicle_type_code_3,
    c.vehicle_type_code_4,
    c.vehicle_type_code_5,
    c.number_of_cyclist_injured,
    c.number_of_cyclist_killed,
    c.number_of_motorist_injured,
    c.number_of_motorist_killed,
    c.number_of_pedestrians_injured,
    c.number_of_pedestrians_killed,
    c.number_of_persons_injured,
    c.number_of_persons_killed
  FROM
    table_20k_crashes c
  WHERE
    ((vehicle_type_code_1 = ':vehicle-type')
    OR
     (vehicle_type_code_2 = ':vehicle-type')
    OR
     (vehicle_type_code_3 = ':vehicle-type')
    OR
     (vehicle_type_code_4 = ':vehicle-type')
    OR
     (vehicle_type_code_5 = ':vehicle-type'))
  AND
    ((date <= date ':end-date')
    AND
     (date >= date ':start-date'))")
