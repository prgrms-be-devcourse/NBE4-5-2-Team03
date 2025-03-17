import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  /* CAUTION!!! ALL TYPECHECKS WILL BE IGNORED WHEN BUILDING!!! */
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
};

export default nextConfig;
