SELECT
    SUM(CAST(t.job_amount AS DECIMAL(18,2))) AS total_job_amount
FROM (
    SELECT
        p.*,
        ROW_NUMBER() OVER (
            PARTITION BY COALESCE(
                NULLIF(p.bid_id,''),
                NULLIF(p.buildingconnected_id,'')
            )
            ORDER BY p.date DESC, p.id DESC
        ) AS rn
    FROM pipeline_bids p
    WHERE p.business_unit_id = ?
) t
WHERE t.rn = 1
  AND t.status = 8
  AND t.date >= CURDATE() - INTERVAL 30 DAY
