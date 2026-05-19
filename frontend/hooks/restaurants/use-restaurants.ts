"use client";
import { useEffect, useState } from "react";
import type { Restaurant } from "@/types";
import { fetchRestaurants } from "@/services/restaurants";

export function useRestaurants() {
  const [data, setData] = useState<Restaurant[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchRestaurants()
      .then((res) => {
        if (!cancelled) {
          setData(res);
          setError(null);
        }
      })
      .catch((e) => {
        if (!cancelled) setError(e?.message || "Failed to load restaurants");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [version]);

  function refresh() {
    setVersion((v) => v + 1);
  }

  return { data, loading, error, refresh };
}
