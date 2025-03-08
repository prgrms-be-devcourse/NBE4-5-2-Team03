"use client";

import { useEffect, useState } from "react";

interface Review {
    id: number;
    nickname: string;
    rating: number;
    content: string;
}

export function MovieReviewsPage() {
    const { movieId } = useParams();
    
}