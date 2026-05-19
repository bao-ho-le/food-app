"use client";
import { useState } from "react";
import { createRestaurant, type CreateRestaurantInput } from "@/services/restaurants";
import type { Restaurant } from "@/types";

export function useCreateRestaurant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function mutate(input: CreateRestaurantInput): Promise<Restaurant> {
    setLoading(true);
    setError(null);
    try {
      const res = await createRestaurant(input);
      return res;
    } catch (e: any) {
      setError(e?.message || "Create failed");
      throw e;
    } finally {
      setLoading(false);
    }
  }

  return { mutate, loading, error };
}

