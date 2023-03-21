<?xml version='1.0' encoding='utf-8'?>
<algebra>
  <basicAlgebra>
    <algName>Z_2^2</algName>
    <cardinality>4</cardinality>
    <operations>
      <op>
        <opSymbol>
          <opName>q</opName>
          <arity>2</arity>
        </opSymbol>
        <opTable>
          <intArray>
            <row r="[0]">0,1,2,3</row>
            <row r="[1]">1,0,3,2</row>
            <row r="[2]">2,3,0,1</row>
            <row r="[3]">3,2,1,0</row>         
          </intArray>
        </opTable>
      </op>
      <op>
        <opSymbol>
          <opName>d</opName>
          <arity>3</arity>
        </opSymbol>
        <opTable>
          <intArray>
            <row r="[0,0]">0,0,0,0</row>
            <row r="[0,1]">0,1,1,1</row>
            <row r="[0,2]">0,1,2,2</row>
            <row r="[0,3]">0,1,2,3</row>
            <row r="[1,0]">0,1,1,1</row>
            <row r="[1,1]">1,1,1,1</row>
            <row r="[1,2]">1,1,2,2</row>
            <row r="[1,3]">1,1,2,3</row>
            <row r="[2,0]">0,1,2,2</row>
            <row r="[2,1]">1,1,2,2</row>
            <row r="[2,2]">2,2,2,2</row>
            <row r="[2,3]">2,2,2,3</row>
            <row r="[3,0]">0,1,2,3</row>
            <row r="[3,1]">1,1,2,3</row>
            <row r="[3,2]">2,2,2,3</row>
            <row r="[3,3]">3,3,3,3</row>
          </intArray>
        </opTable>
      </op>
    </operations>
  </basicAlgebra>
</algebra>