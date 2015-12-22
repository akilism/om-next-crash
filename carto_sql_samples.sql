--CartoDb sql table query raw crash data. All Relevant columns.

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
JOIN
  nyc_borough b

WHERE
  contributing_factor_vehicle_1 = 'Unspecified'
  AND
  borough = 'QUEENS'



  SELECT
    cartodb_id,
    borough,
    identifier,
    the_geom
  FROM
    nyc_borough



WITH current_geometry AS (
  SELECT
    cartodb_id,
    borough,
    identifier,
    the_geom
  FROM
    nyc_borough
  WHERE
    identifier = 4
)
SELECT
  c.cartodb_id,
  c.borough,
  c.contributing_factor_vehicle_1,
  c.contributing_factor_vehicle_2,
  c.cross_street_name,
  c.off_street_name,
  c.on_street_name,
  c.zip_code,
  c.date,
  c.time,
  c.number_of_persons_injured,
  c.number_of_persons_killed
FROM
  table_20k_crashes c
WHERE
  ST_Within(c.the_geom, (SELECT the_geom from current_geometry ORDER BY cartodb_id DESC LIMIT 1))


WITH distinct_identifier AS (
)
SELECT

  SELECT DISTINCT
    borough,
    identifier
  FROM
    nyc_borough


WITH current_geometry AS (
  SELECT
    cartodb_id,
    borough,
    identifier,
    the_geom
  FROM
    nyc_borough
  WHERE
    identifier = 4
)
SELECT
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
  ST_Within(c.the_geom, (SELECT the_geom from current_geometry ORDER BY cartodb_id DESC LIMIT 1))


  SELECT
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


--select all factors

WITH all_factors as (
  SELECT
    contributing_factor_vehicle_1 as factor
  FROM
    table_20k_crashes
  UNION ALL
  SELECT
    contributing_factor_vehicle_2 as factor
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    contributing_factor_vehicle_3 as factor
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    contributing_factor_vehicle_4 as factor
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    contributing_factor_vehicle_5 as factor
  FROM
    table_20k_crashes
)
select
  count(af.factor) as count_factor,
  af.factor
from
  all_factors af
group by
  af.factor
order by
  count_factor desc


WITH all_vehicle_types as (
  SELECT
    vehicle_type_code_1 as vehicle_type_code
  FROM
    table_20k_crashes
  UNION ALL
  SELECT
    vehicle_type_code_2 as vehicle_type_code
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    vehicle_type_code_3 as vehicle_type_code
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    vehicle_type_code_4 as vehicle_type_code
  FROM
    table_20k_crashes
    UNION ALL
  SELECT
    vehicle_type_code_5 as vehicle_type_code
  FROM
    table_20k_crashes
)
select
  count(avt.vehicle_type_code) as count_vehicle_type_code,
  avt.vehicle_type_code
from
  all_vehicle_types avt
group by
  avt.vehicle_type_code
order by
  count_vehicle_type_code desc
