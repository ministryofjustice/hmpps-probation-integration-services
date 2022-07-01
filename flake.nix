{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/master";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }@inputs:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
        };
        x86pkgs = import nixpkgs { system = "x86_64-darwin"; };
      in
      {
        devShell = with pkgs; mkShell {
          inherit system;
          packages = [
            ktlint
            kotlin-language-server
            nodePackages.prettier
            yamllint
            x86pkgs.yq
          ];
        };
      }
    );
}
