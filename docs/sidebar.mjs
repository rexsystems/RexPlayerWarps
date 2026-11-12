const root = "rexplayerwarps";

export const rexplayerwarps = {
  label: "RexPlayerWarps",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Guides",
      items: [
        { label: "Warps", slug: `${root}/guides/warps` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Categories", slug: `${root}/configuration/categories` },
        { label: "Storage", slug: `${root}/configuration/storage` },
        { label: "Messages", slug: `${root}/configuration/messages` },
        { label: "Menu", slug: `${root}/configuration/gui` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
        { label: "Placeholders", slug: `${root}/reference/placeholders` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
