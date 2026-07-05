import type { PaginationLinks } from '@/models/Http';

export function parseLinkHeader(linkHeader: string): PaginationLinks {
  const links: PaginationLinks = {};

  if (!linkHeader) return links;

  const linkRegex = /<([^>]+)>;\s*rel="([^"]+)"/g;
  let match;

  while ((match = linkRegex.exec(linkHeader)) !== null) {
    const url = match[1];
    const rel = match[2];

    if (rel === 'first' || rel === 'last' || rel === 'next' || rel === 'prev') {
      links[rel] = url;
    }
  }

  return links;
}
