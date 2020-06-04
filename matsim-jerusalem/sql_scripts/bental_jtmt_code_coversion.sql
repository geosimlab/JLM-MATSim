--
-- PostgreSQL database dump
--

-- Dumped from database version 10.12
-- Dumped by pg_dump version 10.12

-- Started on 2020-06-04 10:12:53

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 225 (class 1259 OID 43384)
-- Name: bental_jtmt_code_conversion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bental_jtmt_code_conversion (
    usg_group integer NOT NULL,
    usg_code integer NOT NULL,
    count integer NOT NULL,
    purp_1 integer,
    purp_2 integer,
    purp_3 integer,
    purp_4 integer,
    purp_5 integer,
    purp_6 integer,
    purp_7 integer,
    purp_8 integer
);


ALTER TABLE public.bental_jtmt_code_conversion OWNER TO postgres;

-- Completed on 2020-06-04 10:12:53

--
-- PostgreSQL database dump complete
--

