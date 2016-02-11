(ns ta-crash.sql-queries
  (:require-macros [sqlize.core :refer [def-sql-query]])
  (:require [clojure.string :as string]))

;;; These return query functions

(def-sql-query "-- name: distinct-borough
  -- Selects distinct boroughs
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_borough
  ORDER BY
    identifier")

(def-sql-query "-- name: distinct-city-council
  -- Selects distinct city council districts
  SELECT DISTINCT
    identifier
  FROM
    nyc_city_council
  ORDER BY
    identifier")

(def-sql-query "-- name: distinct-community-board
  -- Selects distinct community boards
  SELECT DISTINCT
    identifier
  FROM
    nyc_community_board
  ORDER BY
    identifier")

(def-sql-query "-- name: distinct-neighborhood
  -- Selects distinct neighborhoods
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_neighborhood
  ORDER BY
    borough, identifier")

(def-sql-query "-- name: distinct-precinct
  -- Selects distinct nypd precincts
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_nypd_precinct
  ORDER BY
    borough, identifier")

(def-sql-query "-- name: distinct-zip-code
  -- Selects distinct zip codes
  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_zip_codes
  ORDER BY
    borough, identifier")

(def-sql-query "--name: date-bounds
  -- Selects max and min date from all crashes
  SELECT
    MAX(date) as max_date,
    MIN(date) as min_date
  FROM
    table_20k_crashes")

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
    COUNT(af.factor) as count_factor,
    af.factor
  FROM
    all_factors af
  GROUP BY
    af.factor
  ORDER BY
    count_factor desc")

(def-sql-query "-- name: all-factors-date-by-area
  -- Counts all factors for a given date range filtered by some geometry table.
  WITH all_factors as (
    SELECT
      c.contributing_factor_vehicle_1 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    UNION ALL
    SELECT
      c.contributing_factor_vehicle_2 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_3 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_4 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_5 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
  )
  SELECT
    COUNT(af.factor) as count_factor,
    af.factor
  FROM
    all_factors af
  GROUP BY
    af.factor
  ORDER BY
    count_factor desc")

(def-sql-query "-- name: all-factors-date-filtered
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
    AND
      (:filter-col > 0)
    UNION ALL
    SELECT
      contributing_factor_vehicle_2 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      contributing_factor_vehicle_3 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      contributing_factor_vehicle_4 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      contributing_factor_vehicle_5 as factor
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
  )
  SELECT
    COUNT(af.factor) as count_factor,
    af.factor
  FROM
    all_factors af
  GROUP BY
    af.factor
  ORDER BY
    count_factor desc")

(def-sql-query "-- name: all-factors-date-by-area-filtered
  -- Counts all factors for a given date range filtered by some geometry table.
  WITH all_factors as (
    SELECT
      c.contributing_factor_vehicle_1 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
    UNION ALL
    SELECT
      c.contributing_factor_vehicle_2 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_3 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_4 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      c.contributing_factor_vehicle_5 as factor
    FROM
      table_20k_crashes c
    JOIN
      :geo-table a
    ON
      (ST_Within(c.the_geom, a.the_geom) AND (a.identifier = :identifier))
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
  )
  SELECT
    COUNT(af.factor) as count_factor,
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
    COUNT(avt.vehicle_type) as count_vehicle_type,
    avt.vehicle_type
  from
    all_vehicle_types avt
  group by
    avt.vehicle_type
  order by
    count_vehicle_type desc")

(def-sql-query "-- name: all-vehicle-types-date-filtered
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
    AND
      (:filter-col > 0)
    UNION ALL
    SELECT
      vehicle_type_code_2 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      vehicle_type_code_3 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      vehicle_type_code_4 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
      UNION ALL
    SELECT
      vehicle_type_code_5 as vehicle_type
    FROM
      table_20k_crashes
    WHERE
      (date <= date ':end-date')
    AND
      (date >= date ':start-date')
    AND
      (:filter-col > 0)
  )
  select
    COUNT(avt.vehicle_type) as count_vehicle_type,
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
    COUNT(c.cartodb_id) as total_crashes,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed
  FROM
    table_20k_crashes c
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')")

(def-sql-query "--name: stats-date-by-area
  -- Counts all death / injury stats for a given date range filtered by some geometry table
  SELECT
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  JOIN
    :geo-table a
  ON
    ST_Within(c.the_geom, a.the_geom)
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (a.identifier = :identifier)")

(def-sql-query "-- name: stats-date-filtered
  -- Counts all death / injury stats for a given date range
  SELECT
    COUNT(c.cartodb_id) as total_crashes,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed
  FROM
    table_20k_crashes c
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (:filter-col > 0)")

(def-sql-query "--name: stats-date-by-area-filtered
  -- Counts all death / injury stats for a given date range filtered by some geometry table
  SELECT
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  JOIN
    :geo-table a
  ON
    ST_Within(c.the_geom, a.the_geom)
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (a.identifier = :identifier)
  AND
    (:filter-col > 0)")

(def-sql-query "--name: crashes-by-date
  -- Selects crashes for map by date.
  SELECT
    c.the_geom,
    c.the_geom_webmercator,
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  GROUP BY
    c.the_geom, c.the_geom_webmercator")

(def-sql-query "--name: crashes-by-date-area
  -- Selects crashes for map by area and date.
  SELECT
    c.the_geom,
    c.the_geom_webmercator,
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  JOIN
    :geo-table a
  ON
    ST_Within(c.the_geom, a.the_geom)
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (a.identifier = :identifier)
  GROUP BY
    c.the_geom, c.the_geom_webmercator")

(def-sql-query "--name: crashes-by-date-filtered
  -- Selects crashes for map by date.
  SELECT
    c.the_geom,
    c.the_geom_webmercator,
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (:filter-col > 0)
  GROUP BY
    c.the_geom, c.the_geom_webmercator")

(def-sql-query "--name: crashes-by-date-area-filtered
  -- Selects crashes for map by area and date.
  SELECT
    c.the_geom,
    c.the_geom_webmercator,
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  JOIN
    :geo-table a
  ON
    ST_Within(c.the_geom, a.the_geom)
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (:filter-col > 0)
  AND
    (a.identifier = :identifier)
  GROUP BY
    c.the_geom, c.the_geom_webmercator")

(def-sql-query "--name: intersections-by-date-area-with-order
  --Select all intersections filtered by area and date and order by a col
  SELECT
    concat_ws(',', c.latitude, c.longitude) as pos,
    concat_ws(',', c.on_street_name, c.cross_street_name) as streets,
    c.the_geom,
    COUNT(c.cartodb_id) as total_crashes,
    SUM(c.number_of_cyclist_injured) as cyclist_injured,
    SUM(c.number_of_cyclist_killed) as cyclist_killed,
    SUM(c.number_of_motorist_injured) as motorist_injured,
    SUM(c.number_of_motorist_killed) as motorist_killed,
    SUM(c.number_of_pedestrians_injured) as pedestrians_injured,
    SUM(c.number_of_pedestrians_killed) as pedestrians_killed,
    SUM(c.number_of_persons_injured) as persons_injured,
    SUM(c.number_of_persons_killed) as persons_killed,
    ((SUM(c.number_of_persons_killed) * 2.75) +
     (SUM(c.number_of_persons_injured) * 1.5) +
     (COUNT(c.cartodb_id) * 0.75)) as dval,
    SUM(CASE WHEN c.number_of_persons_injured > 0 THEN 1 ELSE 0 END) AS total_crashes_with_injury,
    SUM(CASE WHEN c.number_of_persons_killed > 0 THEN 1 ELSE 0 END) AS total_crashes_with_death
  FROM
    table_20k_crashes c
  JOIN
    :geo-table a
  ON
    ST_Within(c.the_geom, a.the_geom)
  WHERE
    (date <= date ':end-date')
  AND
    (date >= date ':start-date')
  AND
    (a.identifier = :identifier)
  GROUP BY
    c.the_geom, c.latitude, c.longitude, c.on_street_name, c.cross_street_name
  ORDER BY
    :order-col :order-dir")


;;; Geo-table formatting

(defn get-geo-table [type]
  (condp = type
    :borough "nyc_borough"
    :city-council "nyc_city_council"
    :community-board "nyc_community_board"
    :neighborhood "nyc_neighborhood"
    :precinct "nyc_nypd_precinct"
    :zip-code "nyc_zip_codes"))

(defn get-geo-identifier [identifier]
  (if (string? identifier)
    (str "'" identifier "'")
    identifier))

;;; Query Fetching Functions

(def not-nil? (complement nil?))

(defn get-filter-col
  [stat]
  (condp = stat
    :total-killed "number_of_persons_killed"
    :total-injured "number_of_persons_injured"
    :total-persons-killed "number_of_persons_killed"
    :total-pedestrian-killed "number_of_pedestrians_killed"
    :total-cyclist-killed "number_of_cyclist_killed"
    :total-motorist-killed "number_of_motorist_killed"
    :total-persons-injured "number_of_persons_injured"
    :total-pedestrian-injured "number_of_pedestrians_injured"
    :total-cyclist-injured "number_of_cyclist_injured"
    :total-motorist-injured "number_of_motorist_injured"))

(defn pick-query
  [params queries]
  (let [{:keys [by-date by-date-filtered by-date-area by-date-area-filtered]} queries
        {:keys [start-date end-date active-area active-stat]} params
        {:keys [area-type identifier]} active-area]
    (cond
     (and (or (= "citywide" (:identifier active-area)) (empty? active-area))
          (or (= :total-crashes active-stat) (nil? active-stat)))
      (by-date
        {:end-date end-date
         :start-date start-date})
     (and (or (= "citywide" (:identifier active-area)) (empty? active-area)) (not-nil? active-stat))
      (by-date-filtered
        {:end-date end-date
         :start-date start-date
         :filter-col (get-filter-col active-stat)})
     (and (not (= "citywide" (:identifier active-area))) (or (= :total-crashes active-stat) (nil? active-stat)))
      (by-date-area
        {:end-date end-date
         :start-date start-date
         :geo-table (get-geo-table area-type)
         :identifier (get-geo-identifier identifier)})
     (and (not (= "citywide" (:identifier active-area))) (not-nil? active-stat))
      (by-date-area-filtered
        {:end-date end-date
         :start-date start-date
         :geo-table (get-geo-table area-type)
         :identifier (get-geo-identifier identifier)
         :filter-col (get-filter-col active-stat)}))))

(defmulti get-query (fn [type _] type))

(defmethod get-query :crashes
  [_ params]
  (pick-query params {:by-date crashes-by-date
                      :by-date-filtered crashes-by-date-filtered
                      :by-date-area crashes-by-date-area
                      :by-date-area-filtered crashes-by-date-area-filtered}))

(defmethod get-query :factors
  [_ params]
  (pick-query params {:by-date all-factors-date
                      :by-date-filtered all-factors-date-filtered
                      :by-date-area all-factors-date-by-area
                      :by-date-area-filtered all-factors-date-by-area-filtered
                      }))

(defmethod get-query :vehicles
  [_ params]
  (pick-query params {:by-date all-vehicle-types-date
                      :by-date-filtered all-vehicle-types-date-filtered
                      :by-date-area crashes-by-date-area
                      :by-date-area-filtered crashes-by-date-area-filtered}))

(defmethod get-query :stats
  [_ params]
  (pick-query params {:by-date stats-date
                      :by-date-filtered stats-date-filtered
                      :by-date-area stats-date-by-area
                      :by-date-area-filtered stats-date-by-area-filtered}))

(defmethod get-query :default
  [id params]
  false)
